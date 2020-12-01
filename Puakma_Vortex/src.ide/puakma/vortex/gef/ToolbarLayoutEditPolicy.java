package puakma.vortex.gef;

import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.OrderedLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.gef.requests.GroupRequest;

public abstract class ToolbarLayoutEditPolicy extends OrderedLayoutEditPolicy
{
  private IFigure layoutTargetFeedback;

  private static final int FEEDBACK_WIDTH = 4;

  protected IFigure createLayoutTargetFeedback()
  {
    Shape f = new RectangleFigure();
    f.setBackgroundColor(ColorConstants.black);
    addFeedback(f);
    return f;
  }

  protected IFigure getLayoutTargetFeedback()
  {
    if(layoutTargetFeedback == null)
      layoutTargetFeedback = createLayoutTargetFeedback();
    return layoutTargetFeedback;
  }

  protected void showLayoutTargetFeedback(Request request)
  {
    // le code est tr�s long mais le but est simple: positionner correctement le
    // rectangle de feedback.

    ToolbarLayout layout = (ToolbarLayout) ((AbstractGraphicalEditPart) getHost())
        .getContentPane().getLayoutManager();
    boolean verticalLayout = !layout.isHorizontal();
    IFigure l = getLayoutTargetFeedback();
    AbstractGraphicalEditPart after = (AbstractGraphicalEditPart) getInsertionReference(request);

    Point loc = null;
    Dimension size = null;

    if(after == null) {// feedback � placer apr�s toutes les figures enfants
      if(getHost().getChildren().size() == 0) {// il n'y a pas encore d'enfants
        Figure contentPane = (Figure) ((AbstractGraphicalEditPart) getHost())
            .getContentPane();
        int mid = verticalLayout ? contentPane.getBounds().height / 2 + contentPane.getLocation().y
                                 : contentPane.getBounds().width  / 2 + contentPane.getLocation().x;
        loc = verticalLayout ? new Point(contentPane.getLocation().x, mid)
                             : new Point(mid, contentPane.getLocation().y);
        size = verticalLayout ? new Dimension(contentPane.getSize().width, FEEDBACK_WIDTH)
                              : new Dimension(FEEDBACK_WIDTH, contentPane.getSize().height);
      }
      else {
        // apr�s le dernier enfant
        int lastIndex = getHost().getChildren().size() - 1;
        Figure last = (Figure) ((GraphicalEditPart) getHost().getChildren().get(lastIndex)).getFigure();
        loc = last.getLocation();
        last.translateToAbsolute(loc);
        loc = verticalLayout ? loc.getTranslated(0, last.getSize().height) : loc
            .getTranslated(last.getSize().width, 0);
        size = verticalLayout ? new Dimension(last.getBounds().width, FEEDBACK_WIDTH)
            : new Dimension(FEEDBACK_WIDTH, last.getBounds().height);
      }
    }
    else {
      // feedback � placer juste avant la figure de after
      int indexAfter = getHost().getChildren().indexOf(after);
      IFigure f = after.getFigure();

      if(indexAfter == 0) {// feedback � placer au d�but
        Figure first = (Figure) ((GraphicalEditPart) getHost().getChildren().get(0))
            .getFigure();
        loc = first.getLocation();
        first.translateToAbsolute(loc);
        loc = verticalLayout ? loc.getTranslated(0, -FEEDBACK_WIDTH) : loc
            .getTranslated(-FEEDBACK_WIDTH, 0);
        size = verticalLayout ? new Dimension(first.getBounds().width, FEEDBACK_WIDTH)
            : new Dimension(FEEDBACK_WIDTH, first.getBounds().height);
      }
      else {
        Figure prec = (Figure) ((GraphicalEditPart) getHost().getChildren().get(indexAfter - 1)).getFigure();
        Figure next = (Figure) ((GraphicalEditPart) getHost().getChildren().get(indexAfter)).getFigure();
        if(verticalLayout) {
          int minPrec = prec.getLocation().y + prec.getBounds().height;
          int maxNext = next.getLocation().y;
          int interval = minPrec - maxNext;
          int midInterval = minPrec + interval / 2;
          loc = new Point(prec.getBounds().x, midInterval);
          prec.translateToAbsolute(loc);
          size = new Dimension(prec.getSize().width, FEEDBACK_WIDTH);
        }
        else {
          int maxPrec = prec.getLocation().x + prec.getBounds().width;
          int minNext = next.getLocation().x;
          int interval = minNext - maxPrec;
          int midInterval = maxPrec + interval / 2;
          loc = new Point(midInterval, prec.getBounds().y);
          prec.translateToAbsolute(loc);
          size = new Dimension(FEEDBACK_WIDTH, prec.getSize().height);
        }
      }
    }

    l.setBounds(new Rectangle(loc, size));
  }

  protected void eraseLayoutTargetFeedback(Request request)
  {
    if(layoutTargetFeedback != null)
      removeFeedback(layoutTargetFeedback);
    layoutTargetFeedback = null;
  }

  protected EditPart getInsertionReference(Request request)
  {
    EditPart insertion = null;

    ToolbarLayout layout = (ToolbarLayout) ((AbstractGraphicalEditPart) getHost())
        .getContentPane().getLayoutManager();
    boolean verticalLayout = !layout.isHorizontal();

    int location = verticalLayout ? ((DropRequest) request).getLocation().y
        : ((ChangeBoundsRequest) request).getLocation().x;

    List children = ((GraphicalEditPart) getHost()).getContentPane().getChildren();
    if(children.size() == 0)
      return null;

    Iterator it = children.iterator();
    IFigure childFigure = null;
    int middleChildFigure = 0;
    while(it.hasNext()) {
      childFigure = (IFigure) it.next();
      middleChildFigure = verticalLayout ? childFigure.getBounds().y
                                           + childFigure.getBounds().height / 2
          : childFigure.getBounds().x + childFigure.getBounds().width / 2;
      if(location < middleChildFigure)
        break;
    }
    if(location < middleChildFigure)
    // sortie de boucle via break.
    // il faut placer l'EditPart juste avant l'enfant courant.
    {
      IFigure partFigure = childFigure;
      insertion = (EditPart) getHost().getViewer().getVisualPartMap().get(partFigure);
    }
    else
      // sortie de boucle via while.
      // il faut placer l'EditPart apr�s tous les enfants.
      insertion = null;

    // Il faut prendre le premier EditPart apr�s insertion qui ne se trouve pas
    // dans
    // la liste des EditParts concern�s par la requ�te.
    // En effet, que la requ�te soit de type add children ou move children, il
    // faut s'assurer
    // que l'EditPart de r�f�rence pour l'insertion des childs
    // appartiendra encore � la liste des enfants de l'h�te
    // au moment de l'ex�cution de la commande. Si l'EditPart de r�f�rence
    // appartenait
    // � la liste des children � ajouter, il pourrait subir un orphan avant
    // l'ex�cution de la
    // commande. Il faut �viter cela.
    EditPart after = null;
    if(insertion == null || !(request instanceof GroupRequest))
      after = insertion;
    else {
      List requestChildren = ((GroupRequest) request).getEditParts();
      List hostChildren = getHost().getChildren();
      int indexAfter = hostChildren.indexOf(insertion);
      while(indexAfter < hostChildren.size()
            && requestChildren.contains(hostChildren.get(indexAfter)))
        ++indexAfter;
      if(indexAfter == hostChildren.size())
        after = null;
      else
        after = (EditPart) hostChildren.get(indexAfter);
    }

    return after;
  }
}
