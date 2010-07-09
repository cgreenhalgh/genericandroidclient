
package uk.ac.horizon.ug.exploding.client.model;
/** 
 * Identity element is ID.
 * 
 * Autogenerated by bean2java.xsl */
public class Message
{

  /** no-arg cons */
  public Message()
  {
  }

  /* implements ., i.e. 
    uk.ac.horizon.ug.exploding.db.Message */
  
  /** internal value - 
   * max length = 20.
  
   */
  protected java.lang.String _ID;
  
  /** getter - 
   * max length = 20.
  
   */
  public java.lang.String getID()
  {
  
    return _ID;
    
  }

  /** setter - 
   * max length = 20.
  
   */
  public void setID(java.lang.String ID)
  {
  
    this._ID = ID;
    
  }

  /** is set?
   */
  public boolean isSetID() {
    return this._ID != null; 
  }

  /** unset
   */
  public void unsetID()  {
    this._ID = null; 
  }

  /** internal value - 
   * max length = 20.
  
   */
  protected java.lang.String _playerID;
  
  /** getter - 
   * max length = 20.
  
   */
  public java.lang.String getPlayerID()
  {
  
    return _playerID;
    
  }

  /** setter - 
   * max length = 20.
  
   */
  public void setPlayerID(java.lang.String playerID)
  {
  
    this._playerID = playerID;
    
  }

  /** is set?
   */
  public boolean isSetPlayerID() {
    return this._playerID != null; 
  }

  /** unset
   */
  public void unsetPlayerID()  {
    this._playerID = null; 
  }

  /** internal value - 
   */
  protected java.lang.String _type;
  
  /** getter - 
   */
  public java.lang.String getType()
  {
  
    return _type;
    
  }

  /** setter - 
   */
  public void setType(java.lang.String type)
  {
  
    this._type = type;
    
  }

  /** is set?
   */
  public boolean isSetType() {
    return this._type != null; 
  }

  /** unset
   */
  public void unsetType()  {
    this._type = null; 
  }

  /** internal value - 
   */
  protected java.lang.String _year;
  
  /** getter - 
   */
  public java.lang.String getYear()
  {
  
    return _year;
    
  }

  /** setter - 
   */
  public void setYear(java.lang.String year)
  {
  
    this._year = year;
    
  }

  /** is set?
   */
  public boolean isSetYear() {
    return this._year != null; 
  }

  /** unset
   */
  public void unsetYear()  {
    this._year = null; 
  }

  /** internal value - 
   */
  protected java.lang.String _title;
  
  /** getter - 
   */
  public java.lang.String getTitle()
  {
  
    return _title;
    
  }

  /** setter - 
   */
  public void setTitle(java.lang.String title)
  {
  
    this._title = title;
    
  }

  /** is set?
   */
  public boolean isSetTitle() {
    return this._title != null; 
  }

  /** unset
   */
  public void unsetTitle()  {
    this._title = null; 
  }

  /** internal value - 
   */
  protected java.lang.String _description;
  
  /** getter - 
   */
  public java.lang.String getDescription()
  {
  
    return _description;
    
  }

  /** setter - 
   */
  public void setDescription(java.lang.String description)
  {
  
    this._description = description;
    
  }

  /** is set?
   */
  public boolean isSetDescription() {
    return this._description != null; 
  }

  /** unset
   */
  public void unsetDescription()  {
    this._description = null; 
  }

  /** internal value - 
   */
  protected java.lang.Long _createTime;
  
  /** getter - 
   */
  public java.lang.Long getCreateTime()
  {
  
    return _createTime;
    
  }

  /** setter - 
   */
  public void setCreateTime(java.lang.Long createTime)
  {
  
    this._createTime = createTime;
    
  }

  /** is set?
   */
  public boolean isSetCreateTime() {
    return this._createTime != null; 
  }

  /** unset
   */
  public void unsetCreateTime()  {
    this._createTime = null; 
  }

  /** internal value - 
   */
  protected java.lang.Boolean _handled;
  
  /** getter - 
   */
  public java.lang.Boolean getHandled()
  {
  
    return _handled;
    
  }

  /** setter - 
   */
  public void setHandled(java.lang.Boolean handled)
  {
  
    this._handled = handled;
    
  }

  /** is set?
   */
  public boolean isSetHandled() {
    return this._handled != null; 
  }

  /** unset
   */
  public void unsetHandled()  {
    this._handled = null; 
  }

  /** internal value - 
   */
  protected java.lang.Long _handledTime;
  
  /** getter - 
   */
  public java.lang.Long getHandledTime()
  {
  
    return _handledTime;
    
  }

  /** setter - 
   */
  public void setHandledTime(java.lang.Long handledTime)
  {
  
    this._handledTime = handledTime;
    
  }

  /** is set?
   */
  public boolean isSetHandledTime() {
    return this._handledTime != null; 
  }

  /** unset
   */
  public void unsetHandledTime()  {
    this._handledTime = null; 
  }

  /** equals */
  public boolean equals(Object o) {
    if (o==null) return false;
    if (!(o instanceof Message)) return false;
    Message oo = (Message)o;
      if (_ID!=oo._ID &&
        (_ID==null || oo._ID==null ||
         !_ID.equals(oo._ID)))
      return false;
    if (_playerID!=oo._playerID &&
        (_playerID==null || oo._playerID==null ||
         !_playerID.equals(oo._playerID)))
      return false;
    if (_type!=oo._type &&
        (_type==null || oo._type==null ||
         !_type.equals(oo._type)))
      return false;
    if (_year!=oo._year &&
        (_year==null || oo._year==null ||
         !_year.equals(oo._year)))
      return false;
    if (_title!=oo._title &&
        (_title==null || oo._title==null ||
         !_title.equals(oo._title)))
      return false;
    if (_description!=oo._description &&
        (_description==null || oo._description==null ||
         !_description.equals(oo._description)))
      return false;
    if (_createTime!=oo._createTime &&
        (_createTime==null || oo._createTime==null ||
         !_createTime.equals(oo._createTime)))
      return false;
    if (_handled!=oo._handled &&
        (_handled==null || oo._handled==null ||
         !_handled.equals(oo._handled)))
      return false;
    if (_handledTime!=oo._handledTime &&
        (_handledTime==null || oo._handledTime==null ||
         !_handledTime.equals(oo._handledTime)))
      return false;

    return true;
  }
  /** hashcode */
  public int hashCode() {
    int val = 0;
      if (_ID!=null) val = val ^ _ID.hashCode();
    if (_playerID!=null) val = val ^ _playerID.hashCode();
    if (_type!=null) val = val ^ _type.hashCode();
    if (_year!=null) val = val ^ _year.hashCode();
    if (_title!=null) val = val ^ _title.hashCode();
    if (_description!=null) val = val ^ _description.hashCode();
    if (_createTime!=null) val = val ^ _createTime.hashCode();
    if (_handled!=null) val = val ^ _handled.hashCode();
    if (_handledTime!=null) val = val ^ _handledTime.hashCode();

    return val;
  }
  /** tostring */
  public String toString() {
    StringBuilder str = new StringBuilder("Message:");
    
    str.append("{");
    
    
	str.append("ID=");
	if (_ID!=null) {
	    str.append(_ID.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("playerID=");
	if (_playerID!=null) {
	    str.append(_playerID.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("type=");
	if (_type!=null) {
	    str.append(_type.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("year=");
	if (_year!=null) {
	    str.append(_year.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("title=");
	if (_title!=null) {
	    str.append(_title.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("description=");
	if (_description!=null) {
	    str.append(_description.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("createTime=");
	if (_createTime!=null) {
	    str.append(_createTime.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("handled=");
	if (_handled!=null) {
	    str.append(_handled.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("handledTime=");
	if (_handledTime!=null) {
	    str.append(_handledTime.toString());
	} else {
	    str.append("null");
	}
    
    str.append("}");

    return str.toString();
  }

}
