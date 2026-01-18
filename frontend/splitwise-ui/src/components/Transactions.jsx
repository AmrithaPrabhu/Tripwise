import { useEffect, useState } from 'react';
import './TransactionVersion.css'
import axiosInstance from '../utils/axiosInstance';
import AddExpenseModal from "../components/AddExpenseModal";

const TransactionsList = ({group, addExpense}) => {
  const [showExpenseModal, setShowExpenseModal] = useState(false);
  const [editingExpense, setEditingExpense] = useState(null);
  const [error, setError] = useState("");
  const [transaction, setTransaction] = useState([{
    amount: 0,
    paidBy: "",
    description: "",
    expenseId: ""
  }]);
  const [showInfo, setShowInfo] = useState(false);
  const [expenseId, setExpenseId] = useState(null);
  const [refreshKey, setRefreshKey] = useState(0);

  const [info, setInfo] = useState([{
    amount: 0,
    fromUser: ""
  }])
  useEffect(()=>{
    const fetchTrans = async () => {
      try{
        const res = await axiosInstance.get(`/groups/${group}/expenses`)
        console.log(res)
        setTransaction(res.data)
      }
      catch(err){
        const msg =
          err.response?.data?.message ||
          "Something went wrong!";

        setError(msg);
      }
    }

    fetchTrans()
  }, [addExpense, refreshKey])
  const showTransactionDetails = async (expenseId) => {
      try{
        const res = await axiosInstance.get(`/groups/${group}/expenses/${expenseId}/shares`)
        console.log(res)
        setInfo(res.data)
        setShowInfo(true)
      }catch(err){
        const msg =
          err.response?.data?.message ||
          "Something went wrong!";

        setError(msg);
      }
  }

  const deleteTransaction = async (expenseId) => {
    try{
      const res = await axiosInstance.delete(`/groups/${group}/expenses/${expenseId}`)
      console.log(res)
      setRefreshKey(prev => prev + 1); 
    }catch(err){
      const msg =
          err.response?.data?.message ||
          "Error deleting expense";

        setError(msg);
    }
  }
  return (
    <div className="transactions">
      <h4>Transactions</h4>
      <div className='transactions-scroll'>
      {transaction.map(tx => (
        <div key={tx.expenseId} className="transaction-row">
          <div>
            <strong>{tx.description}</strong>
            <p>Paid by {tx.paidBy}</p>
          </div>

          <div>
            ₹{tx.amount}
            <button className='info transaction-btn' onClick={() => showTransactionDetails(tx.expenseId)}>Info</button>
            <button className='edit transaction-btn' onClick={() => {
              setEditingExpense(tx);
              setShowExpenseModal(true);
            }}>Edit</button>
            <button className='dlt transaction-btn' onClick={() => deleteTransaction(tx.expenseId)}>Delete</button>
          </div>
        </div>
      ))}
      </div>
      {showInfo && (<div className="modal-overlay">
          <div className="modal">
            {error.length > 0 && <p className='error-text'>{error}</p>}
            <h3></h3>
            {info.map(inf => (
              <div key={inf.fromUser} className="transaction-row">
                <div>
                  <strong>{inf.fromUser}</strong>
                </div>

                <div>
                  <strong>{inf.amount}</strong>
                </div>
            </div>
            ))}
            <div className="modal-actions">
              <button
                className="cancel-btn"
                onClick={() => {
                  setShowInfo(false);
                }}
              >
                Cancel
              </button>

            </div>
          </div>
        </div>)}

        {showExpenseModal && (
          <AddExpenseModal
            groupId={group}
            expense={editingExpense}
            onClose={(updated = false) => {
              setShowExpenseModal(false);
              setEditingExpense(null);

               if (updated) {
                setRefreshKey(prev => prev + 1);
              }
            }}
          />
        )}

    </div>
  );
};
export default TransactionsList