document.addEventListener("DOMContentLoaded", () => {
    const stockInput = document.getElementById("stockSymbol");
    const submitBtn = document.getElementById("submitBtn");

    // Show submit button only when input is non-empty
    stockInput.addEventListener("input", () => {
        if (stockInput.value.trim() !== "") {
            submitBtn.style.display = "block";
        } else {
            submitBtn.style.display = "none";
        }
    });

    // Optional: simple validation before submit
    const form = document.getElementById("stockForm");
    form.addEventListener("submit", (event) => {
        const symbol = stockInput.value.trim();
        if (symbol === "") {
            event.preventDefault();
            alert("Please enter a stock symbol.");
        }
    });
});
