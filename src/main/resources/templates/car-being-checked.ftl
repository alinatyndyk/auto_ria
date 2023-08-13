<!DOCTYPE html>
<html lang="en">
<style>
    .desc {
        background-color: #282c34;
        color: aliceblue;
        border-radius: 5px;
    }
</style>
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<h1 class="header" style="color: #007bff">Your announcement has been suspected</h1>
<p>Hello, ${name}</p>
<p style="color: darkred">Oops... Your announcement has been suspected as it did not pass the profanity filter</p>

<p>Offencive language was found:</p>
<p class="desc">${description}</p>

<p>We are sorry to inform that your announcement is not available on the platform.
    It has been sent to Our managers. We will inform you the moment decision is made</p>
<p>With respect,</p>
<p>AutoRia Team</p>
</body>
</html>