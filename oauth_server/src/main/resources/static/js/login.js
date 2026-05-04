document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");

    form.addEventListener("submit", (event) => {
        const username = document.getElementById("username").value.trim();
        const password = document.getElementById("password").value.trim();

        if (!username || !password) {
            event.preventDefault();
            alert("Both username and password are required.");
        }

        if (password.length < 6) {
            event.preventDefault();
            alert("Password must be at least 6 characters long.");
        }
    });
});
