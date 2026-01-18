import React, { useEffect, useState } from "react";
import "./GroupsAndActivity.css";
import { Link } from "react-router-dom";
import axios from "../utils/axiosInstance"; 
import ConfirmationModal from "./ConfirmationModal";

const GroupsAndActivity = () => {
  const [groups, setGroups] = useState([]);
  const [error, setError] = useState("");
  const [refreshGroups, setRefreshGroups] = useState(0);
  const [showConfirm, setShowConfirm] = useState(false);
  const [groupToDelete, setGroupToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [activities, setActivities] = useState([])

  useEffect(()=>{
      console.log("GroupsAndActivity MOUNTED");
    const fetchGroups = async () => {
        try {
            const res = await axios.get("/groups");
            setGroups(res.data)
        } catch (err) {
            console.log(err);

            const message = err.response?.data?.message ||
              "Login failed. Please try again.";

            setError(message);
        }
    }
    const fetchAuditLogs = async () => {
      try {
            const res = await axios.get("/audit");
            console.log(res)
            setActivities(res.data)
        } catch (err) {
            console.log(err);

            const message = err.response?.data?.message ||
              "Login failed. Please try again.";

            setError(message);
        }
    }
    fetchGroups()
    fetchAuditLogs()
     return () => {
    console.log("GroupsAndActivity UNMOUNTED");
  };
    
  }, [refreshGroups])
  

  
  const [showModal, setShowModal] = useState(false);
  const [groupName, setGroupName] = useState("");
  const [createGroupError, setCreateGroupError] = useState(false)
  const handleCreateGroup = async() => {
    if (groupName.trim().length == 0) {
      setCreateGroupError("Name cannot be empty!")
      return;
    }

    try{
      const res = await axios.post("/groups", {
        groupName: groupName
      })
      setRefreshGroups(prev => prev+1)
    }catch(err){
      const msg = err.response?.data?.message || "Failed to add expense";
      setCreateGroupError(msg);
    }
    setGroupName("");
    setShowModal(false);
  };

 const handleDeleteGroup = async (groupId) => {
    try {
      const res = await axios.delete(`/groups/${groupId}`);
      console.log(res);
      setRefreshGroups(prev => prev+1)
    } catch (err) {
      console.log(err)
      if (err.response?.status === 409) {
        setShowConfirm(true);
      } else {
        alert(err.response?.data?.message || "Delete failed");
      }
    }
  };


  return (
    <div className="ga-container">

      {/* GROUPS */}
      <div className="ga-card">
        <div className="card-header">
          <h3 className="card-title">Groups</h3>
          <button className="create-btn" onClick={() => setShowModal(true)}>
            +
          </button>
        </div>

        <div className="groups-list">
          {groups.map(group => (
            <div key={group.id} className="group-row">
              <Link
                to={`/groups/${group.id}`}
                className="group-name group-link"
                state={{ groupName: group.name }}
              >
                {group.name}
              </Link>

              <div className="group-actions">

                <button
                  className="delete-btn"
                  onClick={() => {
                    setGroupToDelete(group.id);
                    setShowConfirm(false); 
                    handleDeleteGroup(group.id);
                  }}
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* RECENT ACTIVITY */}
      <div className="ga-card">
        <h3 className="card-title">Recent Activity</h3>
        <div className="activity-list">
          {activities.map((activity, index) => (
            <div key={index} className="activity-row">
              {activity}
            </div>
          ))}
        </div>
      </div>

      {/* CREATE GROUP MODAL */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>Create Group</h3>
            {createGroupError.length > 0 && ( <p className="error-text">{createGroupError}</p>)}
            <input
              type="text"
              placeholder="Group name"
              value={groupName}
              onChange={(e) => setGroupName(e.target.value)}
              autoFocus
            />

            <div className="modal-actions">
              <button
                className="cancel-btn"
                onClick={() => {
                  setGroupName("");
                  setShowModal(false);
                }}
              >
                Cancel
              </button>

              <button className="create-group-btn" onClick={handleCreateGroup}>
                Create
              </button>
            </div>
          </div>
        </div>
      )}
      {showConfirm && (
            <ConfirmationModal
              title="Pending Settlements"
              message="This group has unpaid settlements. Deleting it will permanently remove all data. Are you sure you want to continue?"
              confirmText="Delete Group"
              onCancel={() => {
                setShowConfirm(false);
                setGroupToDelete(null);
              }}
              onConfirm={async () => {
                try {
                  setDeleting(true);
                  await axios.delete(
                    `/groups/${groupToDelete}?force=true`
                  );
                  refreshGroups();
                } catch (err) {
                  console.log(err)
                } finally {
                  setDeleting(false);
                  setShowConfirm(false);
                  setGroupToDelete(null);
                }
              }}
              loading={deleting}
            />
    )}

    </div>
  );
};

export default GroupsAndActivity;
