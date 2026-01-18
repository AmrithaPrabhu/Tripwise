import React, { useEffect, useState } from "react";
import "./Summary.css";
import axiosInstance from "../utils/axiosInstance";

const Summary = () => {
  const [owedList, setOwedList] = useState([]);
  const [settleList, setSettleList] = useState([]);

  useEffect(() => {
    const summary = async () => {
      try {
        const owedRes = await axiosInstance.get("/groups/positive-summary");
        setOwedList(owedRes.data);

        const settleRes = await axiosInstance.get("/groups/negative-summary");
        setSettleList(settleRes.data);
      } catch (ex) {
        console.log(ex);
      }
    };
    summary();
  }, []);

  return (
    <div className="summary-container">

      {/* YOU ARE OWED */}
      <div className="summary-card positive">
        <h3 className="card-title">You are owed</h3>

        {owedList.length === 0 ? (
          <p className="empty-state">Nobody owes you yet 🎉</p>
        ) : (
          owedList.map((item) => (
            <div key={item.groupId} className="summary-row">
              <span className="group-name">{item.groupName}</span>
              <span className="amount positive">₹{item.amount}</span>
            </div>
          ))
        )}
      </div>

      {/* YOU NEED TO SETTLE */}
      <div className="summary-card negative">
        <h3 className="card-title">You need to settle</h3>

        {settleList.length === 0 ? (
          <p className="empty-state">You owe no one yet 🎉</p>
        ) : (
          settleList.map((item) => (
            <div key={item.groupId} className="summary-row">
              <span className="group-name">{item.groupName}</span>
              <span className="amount negative">₹{item.amount}</span>
            </div>
          ))
        )}
      </div>

    </div>
  );
};

export default Summary;
