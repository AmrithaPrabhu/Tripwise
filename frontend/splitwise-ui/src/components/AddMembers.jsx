import './AddMembers.css'
import { useState } from "react";
import axiosInstance from '../utils/axiosInstance';

const AddMembers = ({groupId}) => {
  const [email, setEmail] = useState("");
  const [members, setMembers] = useState([]);
  const [showMembers, setShowMembers] = useState(false)
  const [error, setError] = useState("")
  const [notification, setNotification] = useState("");

  const addMember = async () => {
    try{
      if (!email.trim()) {
         setNotification("Add legitimate mail!");
        setTimeout(() => setNotification(""), 3000);
         return;
      }
      let alreadyExists = members.some(
        m => m.email.toLowerCase() === email.toLowerCase()
      );

      if (alreadyExists) {
        setNotification("Member already added!");
        setTimeout(() => setNotification(""), 3000);
        return;
      }

      const newMembers = {
        emailIds: [email]
      }
      console.log(newMembers)
      const res = await axiosInstance.post(`/groups/${groupId}/members`, newMembers)
      setEmail("")
      console.log(res)
      let added = res.data.added
      let notFound = res.data.notFound
      let alreadyMembers = res.data.alreadyMembers
      alreadyExists = notFound.some(
        m => m.toLowerCase() === email.toLowerCase()
      );
      if (alreadyExists) {
        setNotification("User not registered in Tripwise!");
        setTimeout(() => setNotification(""), 3000);
        return;
      }
      alreadyExists = alreadyMembers.some(
        m => m.toLowerCase() === email.toLowerCase()
      );
      if (alreadyExists) {
        setNotification("Member already added!");
        setTimeout(() => setNotification(""), 3000);
        return;
      }
      setNotification("Member is added successful!");
      setTimeout(() => setNotification(""), 3000);
      setEmail("")
    } catch(err){
      console.log(err)
    }
  };
  const getMembers = async () => {
      try{
          const res = await axiosInstance.get(`/groups/${groupId}/members`)
          console.log(res.data)
          setMembers(res.data)
          setShowMembers(true)
          setError("")
      }catch(err){
        const msg =
          err.response?.data?.message ||
          "Something went wrong!";

        setError(msg);
      }
  }
  return (
    <div className="members">
      <input
        placeholder="Enter email"
        type='email'
        value={email}
        onChange={e => setEmail(e.target.value)}
      />
      <button onClick={addMember}>Add</button>
      <button onClick={getMembers} className='btn-dist'>Info</button>

      {showMembers && (<div className="modal-overlay">
          <div className="modal">
            {error.length > 0 && <p className='error-text'>{error}</p>}
            <h3></h3>
            {members.map(inf => (
              <div key={inf.name} className="transaction-row">
                <div>
                  <strong>{inf.name}</strong>
                </div>
            </div>
            ))}
            <div className="modal-actions">
              <button
                className="cancel-btn"
                onClick={() => {
                  setShowMembers(false);
                }}
              >
                Cancel
              </button>

            </div>
          </div>
        </div>)}

        {notification && (
          <div className="notification">
            {notification}
          </div>
        )}

    </div>
  );
};
export default AddMembers;