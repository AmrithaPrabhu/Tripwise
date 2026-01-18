import { useEffect, useState } from "react";
import "./AddExpenseModal.css";
import axiosInstance from "../utils/axiosInstance";

const AddExpenseModal = ({ groupId, expense, onClose }) => {
  const [description, setDescription] = useState("");
  const [amount, setAmount] = useState("");
  const [splitType, setSplitType] = useState("equal");

  const [members, setMembers] = useState([]);
  const [paidBy, setPaidBy] = useState("");
  const [splits, setSplits] = useState({});
  const [error, setError] = useState("")
  // Fetch group members
  useEffect(() => {
    const fetchMembers = async () => {
      try {
        const res = await axiosInstance.get(
          `/groups/${groupId}/members`
        );
        setMembers(res.data);
        console.log(res.data)
        // initialize splits as empty (NOT zero)
        const initialSplits = {};
        res.data.forEach(m => {
          initialSplits[m.userId] = "";
        });
        setSplits(initialSplits);

      } catch (ex) {
        console.log(ex);
      }
    };

    fetchMembers();
  }, [groupId]);
  
  useEffect(() => {
      if (expense) {
        setDescription(expense.description);
        setAmount(expense.amount);
        setPaidBy(expense.paidBy);
      }
}, []);

useEffect(() => {
  if (
    splitType === "equal" &&
    amount &&
    members.length > 0
  ) {
    const total = Number(amount);
    const perHead = total / members.length;

    const newSplits = {};
    members.forEach(m => {
      newSplits[m.userId] = Number(perHead.toFixed(2));
    });

    setSplits(newSplits);
  }
}, [splitType, amount, members]);


  // Handle split type change
  const handleSplitTypeChange = (type) => {
    setSplitType(type);

    // Auto-distribute when switching to unequal
  };

  const handleSave = async () => {
    if (!description || !amount || !paidBy) {
          setError("Please fill all required fields");
          return;
    }

      if (splitType === "unequal") {
        if (Object.values(splits).some(v => v === "")) {
          setError("Please enter amount for all members");
          return;
        }

        const sum = Object.values(splits).reduce((a, b) => Number(a) + Number(b), 0);
        if (sum !== Number(amount)) {
          setError("Split amounts must sum to total amount");
          return;
        }
      }

      console.log(splits)
      const shares = Object.entries(splits).map(([userId, amount]) => ({
        userId,
        amount
      }));

      const payload = {
        paidByUserId: paidBy,
        amount: Number(amount),
        description: description,
        shares: shares
      };

      console.log(payload)
      if(expense){
        try {
          const res = await axiosInstance.put(
            `/groups/${groupId}/expenses/${expense.expenseId}`,
            payload
          );
          console.log(res)
        } catch (ex) {
          console.log(ex);
          setError("Failed to add expense!");
        }
      }else{
        try {
          const res = await axiosInstance.post(
            `/groups/${groupId}/expenses`,
            payload
          );
          console.log(res)
        } catch (ex) {
          console.log(ex);
          setError("Failed to add expense!");
        }
      }
     onClose(true)

  };

  return (
    <div className="modal-backdrop">
      <div className="modal-card">

        <h3>Add Expense</h3>
        {error.length > 0 && (<p className="error-text">{error}</p>)}
        <input
          type="text"
          placeholder="Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />

        <input
          type="number"
          placeholder="Amount"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        />

        <select
          value={paidBy}
          onChange={(e) => setPaidBy(e.target.value)}
        >
          <option value="">Paid by</option>
          {members.map(member => (
            <option key={member.email} value={member.email}>
              {member.name}
            </option>
          ))}
        </select>

        <div className="split-options">
          <label>
            <input
              type="radio"
              checked={splitType === "equal"}
              onChange={() => handleSplitTypeChange("equal")}
            />
            Split equally
          </label>

          <label>
            <input
              type="radio"
              checked={splitType === "unequal"}
              onChange={() => handleSplitTypeChange("unequal")}
            />
            Split unequally
          </label>
        </div>

        {splitType === "unequal" && (
          <div className="unequal-split">
            {members.map(member => (
              <div key={member.userId} className="split-row">
                <span>{member.name}</span>
                <input
                  type="number"
                  placeholder="Amount"
                  value={splits[member.userId] || ""}
                  onChange={(e) =>
                    setSplits({
                      ...splits,
                      [member.userId]: Number(e.target.value)
                    })
                  }
                />
              </div>
            ))}
          </div>
        )}

        <div className="modal-actions">
          <button className="cancel-btn" onClick={onClose}>
            Cancel
          </button>
          <button className="add-expense" onClick={handleSave}>
            Save
          </button>
        </div>

      </div>
    </div>
  );
};

export default AddExpenseModal;
