import "./AuthLogo.css";

const AuthLogo = ({show}) => {
  return (
    <div className={`title dosis-style ${show ? "eyes-closed" : ""}`}>
	Tr
<span className = "blinks">
	<span className="wrap">
	<span className="eye blink"></span>
</span>
	i

</span>
	pw
	<span className = "blinks">
	<span className="wrap">
	<span className="eye blink"></span>
</span>
	i

</span>
	se
</div>
  );
};

export default AuthLogo;
