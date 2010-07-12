/**
 * Copyright 2010 The University of Nottingham
 * 
 * This file is part of GenericAndroidClient.
 *
 *  GenericAndroidClient is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  GenericAndroidClient is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with GenericAndroidClient.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package uk.ac.horizon.ug.exploding.client;

import java.util.HashSet;
import java.util.Set;

import android.location.Location;
import uk.ac.horizon.ug.exploding.client.LoginReplyMessage.Status;

/** Client State bundle.
 * 
 * @author cmg
 *
 */
public class ClientState {
	/** client status */
	private ClientStatus clientStatus;
	/** game status */
	private GameStatus gameStatus;
	/** login status */
	private LoginReplyMessage.Status loginStatus = LoginReplyMessage.Status.NOT_DONE;
	/** login message */
	private String loginMessage;
	/** status changed */
	private boolean statusChanged;
	/** last location */
	private Location lastLocation;
	/** location changed */
	private boolean locationChanged;
	/** current zone */
	private String zoneID;
	/** current zone OrgId */
	private int zoneOrgID;
	/** zone changed */
	private boolean zoneChanged;
	/** listener flags */
	public enum Part {
		STATUS(1), LOCATION(2), ZONE(4), ALL(7);
		private int flag;
		Part(int flag) {
			this.flag = flag;
		}
		public int flag() { return flag; }
	}
	/** server client - for access to cached state */
	private Client cache;
	/** cached types changed */
	private Set<String> changedTypes = new HashSet<String>();
	/** cons */
	public ClientState() {		
	}
	
	/**
	 * @param clientStatus
	 * @param gameStatus
	 * @param loginStatus
	 * @param loginMessage
	 * @param statusChanged
	 * @param lastLocation
	 * @param locationChanged
	 * @param zoneID
	 * @param zoneOrgID
	 * @param zoneChanged
	 * @param cache
	 * @param changedTypes
	 */
	public ClientState(ClientStatus clientStatus, GameStatus gameStatus,
			Status loginStatus, String loginMessage, boolean statusChanged,
			Location lastLocation, boolean locationChanged, String zoneID,
			int zoneOrgID, boolean zoneChanged, Client cache,
			Set<String> changedTypes) {
		super();
		this.clientStatus = clientStatus;
		this.gameStatus = gameStatus;
		this.loginStatus = loginStatus;
		this.loginMessage = loginMessage;
		this.statusChanged = statusChanged;
		this.lastLocation = lastLocation;
		this.locationChanged = locationChanged;
		this.zoneID = zoneID;
		this.zoneOrgID = zoneOrgID;
		this.zoneChanged = zoneChanged;
		this.cache = cache;
		this.changedTypes = changedTypes;
		// copy
		this.changedTypes.addAll(changedTypes);
	}

	public ClientState(ClientStatus clientStatus, GameStatus gameStatus) {
		super();
		this.clientStatus = clientStatus;
		this.gameStatus = gameStatus;
	}
	/**
	 * @return the clientStatus
	 */
	public ClientStatus getClientStatus() {
		return clientStatus;
	}
	/**
	 * @param clientStatus the clientStatus to set
	 */
	public void setClientStatus(ClientStatus clientStatus) {
		this.clientStatus = clientStatus;
		statusChanged = true;
	}
	/**
	 * @return the gameStatus
	 */
	public GameStatus getGameStatus() {
		return gameStatus;
	}
	/**
	 * @param gameStatus the gameStatus to set
	 */
	public void setGameStatus(GameStatus gameStatus) {
		this.gameStatus = gameStatus;
		statusChanged = true;
	}
	
	public LoginReplyMessage.Status getLoginStatus() {
		return loginStatus;
	}
	public void setLoginStatus(LoginReplyMessage.Status loginStatus) {
		this.loginStatus = loginStatus;
		statusChanged = true;
	}
	public String getLoginMessage() {
		return loginMessage;
	}
	public void setLoginMessage(String loginMessage) {
		this.loginMessage = loginMessage;
		statusChanged = true;
	}
	
	public boolean isStatusChanged() {
		return statusChanged;
	}
	public void setStatusChanged(boolean statusChanged) {
		this.statusChanged = statusChanged;
	}
	public Location getLastLocation() {
		return lastLocation;
	}

	public void setLastLocation(Location lastLocation) {
		this.lastLocation = lastLocation;
		locationChanged = true;
	}

	public boolean isLocationChanged() {
		return locationChanged;
	}

	public void setLocationChanged(boolean locationChanged) {
		this.locationChanged = locationChanged;
	}

	public String getZoneID() {
		return zoneID;
	}

	public void setZoneID(String zoneID) {
		this.zoneID = zoneID;
		zoneChanged = true;
	}

	public boolean isZoneChanged() {
		return zoneChanged;
	}

	public void setZoneChanged(boolean zoneChanged) {
		this.zoneChanged = zoneChanged;
	}

	public Client getCache() {
		return cache;
	}

	public void setCache(Client cache) {
		this.cache = cache;
	}

	public Set<String> getChangedTypes() {
		return changedTypes;
	}

	public void setChangedTypes(Set<String> changedTypes) {
		this.changedTypes = changedTypes;
	}

	public ClientState clone() {
		ClientState copy = new ClientState(clientStatus, gameStatus, loginStatus, loginMessage, statusChanged, lastLocation, locationChanged,zoneID, zoneOrgID, zoneChanged, cache, changedTypes);
		statusChanged = false;
		locationChanged = false;
		zoneChanged = false;
		changedTypes.clear();
		return copy;
	}

	/**
	 * @return the zoneOrgID
	 */
	public int getZoneOrgID() {
		return zoneOrgID;
	}

	/**
	 * @param zoneOrgID the zoneOrgID to set
	 */
	public void setZoneOrgID(int zoneOrgID) {
		this.zoneOrgID = zoneOrgID;
		zoneChanged = true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ClientState [cache=" + cache + ", changedTypes=" + changedTypes
				+ ", clientStatus=" + clientStatus + ", gameStatus="
				+ gameStatus + ", lastLocation=" + lastLocation
				+ ", locationChanged=" + locationChanged + ", loginMessage="
				+ loginMessage + ", loginStatus=" + loginStatus
				+ ", statusChanged=" + statusChanged + ", zoneChanged="
				+ zoneChanged + ", zoneID=" + zoneID + ", zoneOrgID="
				+ zoneOrgID + "]";
	}
	
}
