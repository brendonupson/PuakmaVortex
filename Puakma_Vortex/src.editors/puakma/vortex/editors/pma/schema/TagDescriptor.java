package puakma.vortex.editors.pma.schema;


public interface TagDescriptor
{
  public String getName();

  public void setDescription(String description);

  public String getDescription();

  public void parseAttribs(String atts, boolean required);
  
  /**
   * Lists all attribute names.
   */
  public String[] listAttributes();
  
  /**
   * Lists all required attribute names.
   */
  public String[] listRequiredAttributes();
}
