import { useEffect, useState } from 'react';
import './TransactionVersion.css'
import axiosInstance from '../utils/axiosInstance';
const VersionHistory = ({group, addExpense}) => {
  const [activities, setActivities] = useState([])
  const [error, setError] = useState("")
  useEffect(()=>{
    const fetchAuditLogs = async () => {
      try {
            const res = await axiosInstance.get(`/audit/${group}`);
            console.log(res)
            setActivities(res.data)
        } catch (err) {
            console.log(err);

            const message = err.response?.data?.message ||
              "Something went wrong fetching history";

            setError(message);
        }
    }
    fetchAuditLogs()
  }, [addExpense])
  return (
    <div className="history">
      <h4>History</h4>
      {error.length > 0 && <p className='error-text'>{error}</p>}
      <ul>
        {activities.map((h, i) => <li key={i}>{h}</li>)}
      </ul>
    </div>
  );
};
export default VersionHistory