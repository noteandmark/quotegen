// loading-indicator.js

function checkLoading() {
    var loading = /*[[${loading}]]*/ false;

    if (loading) {
        // Show loading indicator
        document.getElementById("loadingIndicator").style.display = "flex";
    } else {
        // Hide loading indicator
        document.getElementById("loadingIndicator").style.display = "none";
    }
}

// Call the function when the page is loaded
document.addEventListener("DOMContentLoaded", checkLoading);