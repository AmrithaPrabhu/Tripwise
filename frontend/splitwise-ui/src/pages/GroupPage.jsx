import { useState } from "react";
import NavBar from "../components/NavBar";
import AddExpenseModal from "../components/AddExpenseModal";
import Transactions from "../components/Transactions";
import VersionHistory from "../components/VersionHistory";
import AddMembers from "../components/AddMembers";
import './GroupPage.css'
import { useNavigate, useParams } from "react-router-dom";
import axiosInstance from "../utils/axiosInstance";
import { useLocation } from "react-router-dom";

const GroupPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const groupName = location.state?.groupName;
  const [showModal, setShowModal] = useState(false);
  const [showBalances, setShowBalances] = useState(false);
  const [showSettlements, setShowSettlements] = useState(false)
  const [activeBalances, setActiveBalances] = useState([])
  const [settledBalances, setSettledBalances] = useState([])
  const [error, setError] = useState("")
  const { groupId } = useParams()
  const [unsettledBalances, setUnsettledBalances] = useState([])
  const [selectedSettlement, setSelectedSettlement] = useState(null);
  const [payAmount, setPayAmount] = useState("");

  const handleExport = async () => {
      try {   
         const res = await axiosInstance.get(
        `/groups/${groupId}/export/excel`,
        { responseType: "blob" }
      );
      console.log(res.headers)
      const blob = new Blob([res.data], {
        type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
      });

      const contentDisposition = res.headers["content-disposition"];
      console.log(contentDisposition)
      let filename = "group_export.xlsx";

      if (contentDisposition) {
        const match = contentDisposition.match(
          /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/
        );

        if (match && match[1]) {
          filename = match[1].replace(/['"]/g, "");
        }
      }

      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = filename;

      document.body.appendChild(a);
      a.click();

      a.remove();
      window.URL.revokeObjectURL(url);
    }catch(err){
      console.log(err)
    }
  }
  const fetchBalances = async () => {
    try{
      const res = await axiosInstance.get(`/groups/${groupId}/balances`)
      const active = [];
      const settled = [];

      res.data.forEach(b => {
        if (b.status === "ACTIVE") {
          active.push(b);
        } else if (b.status === "SETTLED") {
          settled.push(b);
        }
      });

      setActiveBalances(active);
      setSettledBalances(settled);
    }catch(err){
      const msg =
        err.response?.data?.message ||
        "Failed to add expense";

      setError(msg);
    }
  }

  const fetchSettlements = async () => {
    try{
        const res = await axiosInstance.get(`/groups/${groupId}/settlements`)
        setUnsettledBalances(res.data)
        console.log(unsettledBalances)
    } catch(err) {
        const msg =
        err.response?.data?.message ||
        "Failed to add expense";

      setError(msg);
    }
  }
  return (
    <>
      <NavBar />

      <main>
        <div className="group-container">
          <button
                className="back-btn"
                onClick={() => navigate(-1)}
          >
            ← Back
          </button>
          <h2 className="group-title">{groupName}</h2>

          <div className="group-options">
            <button className="options-btn add-expense"
              onClick={() => setShowModal(true)}
            >
              Add Expense
            </button>
            <button className="options-btn balances-btn" onClick={() => {
                  fetchBalances();
                  setShowBalances(true);
                }}>Balances
            </button>
            <button className="options-btn settle-btn" onClick={() => {
                  fetchSettlements();
                  setShowSettlements(true);
                }}> Settle Up</button>
            <button className="options-btn export-btn" onClick={handleExport}>Export</button>
          </div>

          <AddMembers groupId={groupId} />

          <div className="content-row">
            <Transactions  group = {groupId} addExpense={showModal}/>
            <VersionHistory group = {groupId} addExpense={showModal} />
          </div>
        </div>
      </main>

      {showModal && <AddExpenseModal groupId = {groupId} onClose={() => setShowModal(false)} />}
      {showBalances && (
          <div className="modal-backdrop">
            <div className="modal-card balances-modal">
              {error.length > 0 && <p className="error-text">{error}</p>}
              <h3>Balances</h3>

              <h4>Need to settle</h4>
              {activeBalances.length === 0 && <p>No active balances 🎉</p>}
              {activeBalances.map((b, idx) => (
                <div key={idx} className="balance-row">
                  <span>
                    <strong>{b.fromUser}</strong> owes <strong>{b.toUser}</strong>
                  </span>
                  <span>₹{b.amount}</span>
                </div>
              ))}

              <h4>Settled</h4>
              {settledBalances.length === 0 && <p>No settled balances</p>}
              {settledBalances.map((b, idx) => (
                <div key={idx} className="balance-row settled">
                  <span>
                    <strong>{b.fromUser}</strong> → <strong>{b.toUser}</strong>
                  </span>
                  <span>₹{b.amount}</span>
                </div>
              ))}

              <div className="modal-actions">
                <button
                  className="cancel-btn"
                  onClick={() => setShowBalances(false)}
                >
                  Close
                </button>
              </div>

            </div>
          </div>
    )}
      {showSettlements && (
          <div className="modal-backdrop">
            <div className="modal-card balances-modal">
              {error.length > 0 && <p className="error-text">{error}</p>}
              <h3>Settlements</h3>
              {unsettledBalances.length === 0 && <p>You have no settlements 🎉</p>}
              {unsettledBalances.length > 0 && (
                <div className="balance-row settle-row">

                  {/* FROM USER (fixed) */}
                  <span>
                    <strong>{unsettledBalances[0].fromUser}</strong> pays
                  </span>

                  {/* TO USER (dropdown) */}
                  <select
                    value={selectedSettlement?.id || ""}
                    onChange={(e) => {
                      const settlement = unsettledBalances.find(
                        s => s.id === e.target.value
                      );
                      setSelectedSettlement(settlement);
                      setPayAmount(settlement.amount);
                    }}
                  >
                    <option value="">Select person</option>
                    {unsettledBalances.map(b => (
                      <option key={b.id} value={b.id}>
                        {b.toUser}
                      </option>
                    ))}
                  </select>

                  {/* AMOUNT */}
                  {selectedSettlement && (
                    <>
                      <input
                        type="number"
                        value={payAmount}
                        max={selectedSettlement.amount}
                        min={1}
                        onChange={(e) => {
                          const value = Number(e.target.value);
                          if (value <= selectedSettlement.amount) {
                            setPayAmount(value);
                          }
                        }}
                      />
                      <span className="max-hint">
                        (Max ₹{selectedSettlement.amount})
                      </span>
                      <button
                            className="settle-btn"
                            disabled={payAmount <= 0}
                            onClick={async () => {
                              await axiosInstance.post(
                                `/groups/${groupId}/settlements/${selectedSettlement.id}`,
                                { 
                                  toUser: selectedSettlement.toUserId,
                                  toUserName: selectedSettlement.toUser,
                                  amount: payAmount
                                 }
                              );
                              setShowSettlements(false) 
                              setSelectedSettlement(null)
                              // refresh balances after
                            }}
                          >
                            Pay ₹{payAmount}
                  </button>
                    </>
                  )}

                </div>
              )}

              <div className="modal-actions">
                <button
                  className="cancel-btn"
                  onClick={() => {setShowSettlements(false) 
                    setSelectedSettlement(null)
                  }}
                >
                  Close
                </button>
              </div>

            </div>
          </div>
    )}

    </>
  );
};
export default GroupPage;