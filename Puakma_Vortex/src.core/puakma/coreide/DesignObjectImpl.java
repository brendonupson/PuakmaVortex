/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jun 30, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.CRC32;

import puakma.SOAP.SOAPFaultException;
import puakma.coreide.designer.AppDesigner;
import puakma.coreide.designer.DownloadDesigner;
import puakma.coreide.designer.ApplicationStructureBean.DObject;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.coreide.objects2.ServerDataStatus;
import puakma.utils.io.FileUtils;
import puakma.utils.lang.StringUtil;

/**
 * This is implementation for the {@link DesignObject} interface.
 * 
 * @author Martin Novak
 */
abstract class DesignObjectImpl extends ApplicationObjectImpl implements DesignObject
{
	/**
	 * Maximal length of the name.
	 */
	public static final int MAX_NAME_LENGTH = 30;

	private String contentType;
	private int designType;
	private String inheritFrom;
	private String options = "";
	private String updatedByUser;
	private Date lastUpdateTime;

	/**
	 * Parameters are hold in another object
	 */
	private ParametersImpl parameters = new ParametersImpl(this);
	private Object dataLock = new Object();
	private int designDataSize = 0;
	private int designSourceSize = 0;

	private long dataCrc32;
	private long sourceCrc32;

	DesignObjectImpl(ApplicationImpl application, int designType)
	{
		super(application);

		setDesignType(designType);
	}

	public void download(File file, boolean isSource) throws PuakmaCoreException, IOException
	{
		if(isRemoved() || isNew())
			throw new PuakmaCoreException("Cannot upload data to new or removed object");
		if(file.exists() == false)
			throw new PuakmaCoreException("File " + file + " doesn't exist");
		// CHECK THE FILE FOR WRITING, AND DIRECTORIES
		try {
			FileUtils.checkDirectoryForFile(file);
			if(file.canWrite() == false)
				throw new PuakmaCoreException("Cannot read file " + file);
		}
		catch(FileNotFoundException e1) {
			throw new PuakmaCoreException("Cannot write file. Reason:" + e1.getLocalizedMessage(), e1);
		}

		synchronized(dataLock) {
			DownloadDesigner designer = null;
			FileOutputStream fos = null;
			try {
				designer = application.getDownloadDesigner();
				byte[] b = designer.downloadDesign(getId(), isSource);
				fos = new FileOutputStream(file);
				fos.write(b);
			}
			catch(IOException e) {
				throw e;
			}
			catch(Exception e) {
				PuakmaLibraryManager.log(e);
				throw PuakmaLibraryUtils.handleException("Cannot download file. Reason: " + e.getLocalizedMessage(), e);
			}
			finally {
				try { if(fos != null) fos.close(); } catch(Exception ex) {  }

				application.returnDesigner(designer);
			}
		}
	}

	public void download(OutputStream os, boolean isSource) throws PuakmaCoreException, IOException
	{
		synchronized(dataLock) {
			DownloadDesigner designer = null;
			try {
				designer = application.getDownloadDesigner();
				byte[] b = designer.downloadDesign(getId(), isSource);
				os.write(b);
			}
			catch(IOException e) {
				throw e;
			}
			catch(Exception e) {
				PuakmaLibraryManager.log(e);
				throw PuakmaLibraryUtils.handleException("Cannot download file. Reason: " + e.getLocalizedMessage(), e);
			}
			finally {
				application.returnDesigner(designer);
			}
		}
	}

	public void download(OutputStream os, boolean isSource, long revision) throws PuakmaCoreException, IOException
	{
		if(revision == REV_CURRENT)
			download(os, isSource);
		else
			throw new IllegalStateException("Not implemented yet");
		// TODO: implement revisioning system there
	}

	public String getContentType()
	{
		return contentType;
	}

	public int getDesignType()
	{
		return designType;
	}

	public String getInheritFrom()
	{
		return inheritFrom;
	}

	public String getOptions()
	{
		return options;
	}

	public void setDesignType(int designType)
	{
		if(designType != this.designType) {
			this.designType = designType;
			setDirty(true);
		}
	}

	public void setInheritFrom(String inheritFrom)
	{
		if(StringUtil.compareStrings(inheritFrom, this.inheritFrom) == false) {
			this.inheritFrom = inheritFrom;
			setDirty(true);
		}
	}

	public void setOptions(String options)
	{
		if(options == null)
			throw new IllegalArgumentException("Options cannot be null");

		if(this.options.equals(options) == false) {
			String oldValue = this.options;
			this.options = options;
			setDirty(true);

			fireEvent(PROP_OPTIONS, oldValue, options);
		}
	}

	public String getUrl()
	{
		return null;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	public Date getLastUpdateTime()
	{
		return lastUpdateTime;
	}

	protected void setLastUpdateTime(Date time)
	{
		lastUpdateTime = time;
	}

	public String getUpdatedByUser()
	{
		return updatedByUser;
	}

	protected void setUpdatedByUser(String user)
	{
		updatedByUser = user;
	}

	public void addParameter(String name, String value)
	{
		parameters.addParameter(name, value);
	}

	public String getParameterValue(String name)
	{
		return parameters.getParameterValue(name);
	}

	public void commitParams() throws PuakmaCoreException
	{
		parameters.commitParams();
	}

	public String[] getParameterValues(String name)
	{
		return parameters.getParameterValues(name);
	}

	public String[] listParameters()
	{
		return parameters.listParameters();
	}

	public void setParameters(String name, String[] values)
	{
		parameters.setParameters(name, values);
	}

	public void setParameter(String name, String value)
	{
		parameters.setParameter(name, value);
	}

	public void removeParameter(String name)
	{
		parameters.removeParameter(name);
	}

	public void removeParameterValue(String name, String value)
	{
		parameters.removeParameterValue(name, value);
	}

	public boolean isReservedPageProperty(String name)
	{
		return parameters.isReservedPageProperty(name);
	}

	public boolean isReservedAppProperty(String name)
	{
		return parameters.isReservedAppProperty(name);
	}

	public void upload(InputStream is, boolean isSource) throws PuakmaCoreException
	{
		boolean flushCache = true;
		upload(is, isSource, flushCache);
	}

	public void upload(InputStream is, boolean isSource, boolean flushCache) throws PuakmaCoreException
	{
		if(isRemoved() || isNew())
			throw new PuakmaCoreException("Cannot upload data to new or removed object");

		synchronized(dataLock) {
			DownloadDesigner designer = null;
			try {
				designer = application.getDownloadDesigner();

				// READ INPUT STREAM TO CACHE
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				byte[] data = new byte[1024];
				long[] ret = new long[2];
				while(true) {
					int readBytes = is.read(data);
					if(readBytes == -1)
						break;
					ret[0] += readBytes;
					os.write(data, 0, readBytes);
				}
				data = os.toByteArray();

				// NOW CALCULATE CRC
				CRC32 crc = new CRC32();
				crc.update(data);
				long crcValue = crc.getValue();

				designer.uploadDesign(getId(), isSource, data, flushCache);

				setDesignSize(isSource, data.length);
				setCrc32(crcValue, isSource);
			}
			catch(IOException e) {
				PuakmaLibraryManager.log(e);
				throw new PuakmaCoreException("Cannot upload file. Reason: " + e.getLocalizedMessage(), e);
			}
			catch(Exception e) {
				PuakmaLibraryManager.log(e);
				throw PuakmaLibraryUtils.handleException("Cannot upload file. Reason: " + e.getLocalizedMessage(), e);
			}
			finally {
				application.returnDesigner(designer);
			}
		}
	}

	public void upload(File file, boolean isSource) throws PuakmaCoreException
	{
		boolean flushCache = true;
		upload(file, isSource, flushCache);
	}

	public void upload(File file, boolean isSource, boolean flushCache) throws PuakmaCoreException
	{
		if(isRemoved() || isNew())
			throw new PuakmaCoreException("Cannot upload data to new or removed object");
		if(file.exists() == false)
			throw new PuakmaCoreException("File " + file + " doesn't exist");
		if(file.canRead() == false)
			throw new PuakmaCoreException("Cannot read file " + file);

		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			upload(is, isSource, flushCache);
		}
		catch(FileNotFoundException e) {
			throw new PuakmaCoreException("Cannot find the file " + file.toString(), e);
		}
		finally {
			try {
				if(is != null)
					is.close();
			}
			catch(IOException e) {  }
		}
	}

	public void commit() throws PuakmaCoreException
	{
		if(isRemoved())
			throw new PuakmaCoreException("Cannot commit removed object");
		if(isNew() == false && isWorkingCopy() == false)
			throw new PuakmaCoreException("Cannot commit server object, it has to be working copy");

		synchronized(this) {
			try {
				// SAVE SOME OLD VALUES BEFORE COMMITTING
				String oldName = isWorkingCopy() ? original.getName() : null;
				if(StringUtil.compareStrings(oldName, getName()))
					oldName = null;
				String oldClass = null;
				String oldPackage = null;
				boolean isNew = isNew();
				if(original instanceof JavaObject) {
					JavaObject jo = (JavaObject) original;
					oldClass = jo.getClassName();
					oldPackage = jo.getPackage();
					if(StringUtil.compareStrings(oldClass, jo.getClassName()))
						oldClass = null;
					if(StringUtil.compareStrings(oldPackage, jo.getPackage()))
						oldPackage = null;
				}

				AppDesigner designer = application.getAppDesigner();
				int type = getDesignType();
				if(type == TYPE_JAR_LIBRARY)
					type = TYPE_LIBRARY;

				// HANDLE THE SITUATION WHEN WE DON'T HAVE DESIGN OBJECT NAME WHICH IS
				// QUITE COMMON, AND OK FOR JAVA FILES
				String name = getName();
				if(this instanceof JavaObject && name == null) {
					name = application.generateNameForClass((JavaObject) this);
					this.name = name;
				}
				
				long newId = designer.updateDesignObject(getId(), getApplication().getId(), name,
						type, getContentType(), getDescription(), getOptions(), getInheritFrom());
				// IF THE OBJECT IS NEW, WE SHOULD ALSO UPDATE PARAMETERS
				if(getId() == -1) {
					setId(newId);
					parameters.commitParams();
				}

				// UPDATE USER, AND TIME OF UPDATE
				this.updatedByUser = server.getX500UserName();
				this.lastUpdateTime = Calendar.getInstance().getTime();

				if(isNew() == false)
					((DesignObjectImpl)original).copyFromWorkingCopy(this);

				setValid();
				parameters.setValid();

				// NOW FIRE SOME EVENTS, NOTE THAT NEW OBJECT EVENTS ARE BEING FIRED FROM APPLICATIONIMPL CLASS
				if(isNew == false) {
					DesignObjectImpl ooo = (DesignObjectImpl) original;
					// TODO: remove this event
					ooo.fireUpdateEvent(isNew, oldName, oldPackage, oldClass, false);
				}
			}
			catch(PuakmaCoreException e) {
				throw e;
			}
			catch(Exception e) {
				throw PuakmaLibraryUtils.handleException("Cannot save java object to server", e);
			}
		}
	}

	/**
	 * This function fires all the events after we update design object
	 * 
	 * TODO: This currently doesn't fire anything, we should either remove it, or replace
	 * this event with something else
	 */
	protected void fireUpdateEvent(boolean isNew, String oldName, String oldPackage, String oldClass, boolean isRefresh)
	{
		// NOW IGNORE WHEN THIS DESIGN OBJECT IS NEW
		if(isNew)
			return;

		//    DesignObjectListener[] ls = listListeners();
		//    for(int i = 0; i < ls.length; ++i) {
		//      try {
		//        DesignObjectEventImpl event = new DesignObjectEventImpl(ApplicationObjectEvent.EV_UPDATE, this);
		//        event.oldClassName = oldClass == null ? null : oldClass;
		//        event.oldPackageName = oldPackage == null ? null : oldPackage;
		//        event.oldName = oldName == null ? null : oldName;
		//        ls[i].handleEvent(event);
		//      }
		//      catch(Exception ex) {
		//        PuakmaLibraryManager.log(ex);
		//      }
		//    }

		// AND NOW FIRE APPLICATION EVENT
		if(isNew == false && application != null) { // FOR SURE AND FUTURE [-;
			application.fireUpdateObject(this, isNew, oldName, oldPackage, oldClass, isRefresh);
		}
	}

	void close()
	{
		closing = true;

		try {
			setRemoved();
			setApplication(null);
			super.close();
		}
		finally {
			closing = false;
		}
	}


	public void remove() throws PuakmaCoreException
	{
		if(isNew())
			throw new PuakmaCoreException("Cannot remove new nonexisting object");
		if(isWorkingCopy())
			throw new PuakmaCoreException("Cannot remove working copy");

		synchronized(this) {
			try {
				if(isRemoved() == false) {
					AppDesigner designer = application.getAppDesigner();
					designer.removeDesignObject(getId());
				}
				else assert false : "Should be already removed!!!";
				setRemoved();
				application.notifyRemove(this);
			}
			catch(Exception e) {
				throw PuakmaLibraryUtils.handleException("Cannot remove keyword", e);
			}
		}
	}

	protected void copyFromWorkingCopy(DesignObjectImpl workingCopy)
	{
		super.copyFromWorkingCopy(workingCopy);

		this.options = workingCopy.options;
		this.designType = workingCopy.designType;
		this.contentType = workingCopy.contentType;
		this.inheritFrom = workingCopy.inheritFrom;
		this.lastUpdateTime = workingCopy.lastUpdateTime;
		this.updatedByUser = workingCopy.updatedByUser;

		// PARAMETERS ARE USING IT'S OWN COPYING MECHANISM
	}

	protected void makeCopy(DesignObjectImpl workingCopy, boolean isWorkingCopy)
	{
		super.makeCopy(workingCopy);

		workingCopy.options = this.options;
		workingCopy.designType = this.designType;
		workingCopy.contentType = this.contentType;
		workingCopy.inheritFrom = this.inheritFrom;
		workingCopy.updatedByUser = this.updatedByUser;
		workingCopy.lastUpdateTime = this.lastUpdateTime;

		if(isWorkingCopy)
			workingCopy.parameters = parameters.makeWorkingCopy();
		else
			workingCopy.parameters = parameters.copy(workingCopy, null);
	}

	public void refreshFrom(DObject dbean, RefreshEventInfoImpl info, boolean isNew)
	{
		synchronized(this) {
			assert getId() == getId() : "Identifier has to be the same in the both objects - original and refreshing";

			// AT FIRST WE SHOULD ADD THE REFRESH INFO FOR THIS OBJECT, BUT DO IT ONLY
			// IF IT IS NECESSARY
			// TODO: move this call outside
			if(info != null)
				addToRefreshEvent(dbean, info, isNew);

			boolean nameChange = dbean.name.equals(getName()) == false;
			String oldName = getName();
			if(StringUtil.compareStrings(oldName, dbean.name))
				oldName = null;

			this.setId(dbean.id);
			this.setName(dbean.name);
			this.setDescription(dbean.dobjDescription);
			this.setContentType(dbean.contentType);
			this.setOptions(dbean.options);
			this.setInheritFrom(dbean.dobjInheritFrom);
			this.setDesignSize(true, dbean.designSourceSize);
			this.setDesignSize(false, dbean.designDataSize);
			this.setLastUpdateTime(dbean.lastUpdateTime);
			this.setUpdatedByUser(dbean.updatedBy);
			this.setCrc32(dbean.dataCrc32, false);
			this.setCrc32(dbean.sourceCrc, true);

			parameters.refreshFrom(dbean.parameters);

			if(this instanceof JavaObject == false) {
				boolean dirty;
				if((dirty = isDirty()) == true)
					setDirty(false);

				//      AND FIRE SOME EVENTS
				// AND IF THIS IS INSTANCE OF JAVA OBJECT, LET JAVA OBJECT FIRE EVENT ITSELF
				// TODO: predelat aby se update jenom volal nekde...
				if(isNew == false && dirty)
					fireUpdateEvent(false, oldName, null, null, true);
			}
			// AND SOME SPECIAL CODE FOR JAVA OBJECTS
			else {
				boolean classChange = dbean.className.equals(((JavaObject)this).getClassName()) == false;
				String oldClass = ((JavaObject)this).getClassName();
				boolean packageChange = dbean.packageName.equals(((JavaObject)this).getPackage()) == false;
				String oldPackage = ((JavaObject)this).getPackage();

				((JavaObject)this).setClassName(dbean.className);
				((JavaObject)this).setPackage(dbean.packageName);

				boolean dirty;
				if((dirty = isDirty()) == true)
					setDirty(false);

				// NOW FIRE UPDATE EVENTS
				if(isNew == false && dirty)
					fireUpdateEvent(false, nameChange ? oldName : null, packageChange ? oldPackage : null,
							classChange ? oldClass : null, true);
			}
		}
	}

	private void addToRefreshEvent(DObject dbean, RefreshEventInfoImpl info, boolean isNew)
	{
		if(isNew == false) {
			// IN THE CASE OF POSSIBLE CHANGE, WE SHOULD ALSO CHECK IF THE CHANGE IS NEEDED
			boolean change = false;
			if(getName().equals(dbean.name) == false)
				change = true;
			else if(getCrc32(true) != dbean.sourceCrc)
				change = true;
			else if(getCrc32(false) != dbean.dataCrc32)
				change = true;
			else if(this instanceof JavaObject) {
				JavaObject jo = (JavaObject) this;
				if(StringUtil.compareStrings(jo.getClassName(), dbean.className) == false)
					change = true;
				else if(StringUtil.compareStrings(jo.getPackage(), dbean.packageName) == false)
					change = true;
			}

			if(change) {
				String className = null;
				String packageName = null;
				if(this instanceof JavaObject) {
					JavaObject jo = (JavaObject) this;
					className = jo.getClassName();
					packageName = jo.getPackage();
				}
				long time = 0;   // TODO: CHANGE THIS TO THE VALID TIME!
				info.addChange(this, getName(), getUpdatedByUser(), getCrc32(false),
						getCrc32(true), getDesignSize(false), getDesignSize(true), time,
						className, packageName);
			}
		}
		else
			info.addAdd(this);
	}

	public String toString()
	{
		return name + " [" + getId() + "]";
	}

	public int getDesignSize(boolean isSource)
	{
		return isSource ? designSourceSize : designDataSize;
	}

	void setDesignSize(boolean isSource, int newSize)
	{
		if(isSource)
			designSourceSize = newSize;
		else
			designDataSize = newSize;
	}

	private synchronized void setCrc32(long checksum, boolean isSource)
	{
		if(isSource)
			sourceCrc32 = checksum;
		else
			dataCrc32 = checksum;
	}

	public synchronized long getCrc32(boolean isSource)
	{
		if(isSource)
			return sourceCrc32;
		else
			return dataCrc32;
	}

	public ServerDataStatus getServerStatus() throws PuakmaCoreException, IOException
	{
		if(isNew())
			throw new IllegalStateException("Design object is not on the server");

		ServerDataStatus status = new ServerDataStatus();
		try {
			AppDesigner designer = application.getAppDesigner();
			String[][] s = designer.getDesignObjectsSizeCrc32(new long[] { getId() });
			if(s.length != 1)
				throw new PuakmaCoreException("Illegal response from the server");

			status.setSourceCrc32(Long.parseLong(s[0][0]));
			status.setDataCrc32(Long.parseLong(s[0][1]));
			status.setSourceLength(Long.parseLong(s[0][2]));
			status.setDataLength(Long.parseLong(s[0][3]));
			status.setUpdateTime(Long.parseLong(s[0][4]));
			status.setAuthor(s[0][5]);
		}
		catch(SOAPFaultException ex) {
			throw new PuakmaCoreException(ex);
		}
		return status;
	}

	public ServerDataStatus getKnownServerStatus()
	{
		ServerDataStatus status = new ServerDataStatus();
		status.setSourceCrc32(getCrc32(true));
		status.setDataCrc32(getCrc32(false));
		status.setSourceLength(getDesignSize(true));
		status.setDataLength(getDesignSize(false));
		// TODO: implement also downloading modify time from the server
		status.setUpdateTime(0);
		status.setAuthor(getUpdatedByUser());
		return status;
	}

	public int getParameterCount()
	{
		return parameters.getParameterCount();
	}
}
