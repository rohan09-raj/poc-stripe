# poc-stripe

## Setup
- Add your test API key in server.py and MainActivity.kt
- Open the server.py in VSCode and run the install stripe command `pip3 install stripe` (make sure you have Python and pip3 installed)
- Run the script and server will start running on `port 4242`
- Once the server is running, use ngrok to setup a base url for your server `ngrok http 4242`
- Copy the https url given by ngrok and paste it in `MainActivity.kt` as a value of `BACKEND_URL` in companion object
- Build the Android project and test it on your Android Device/emulator

## Troubleshooting
- Once the ngrok `BACKEND_URL` expires, do update it or use a new server URL by putting it in the field without slash in last
For example - https://852b-103-211-19-38.ngrok.io
- server.py has to be running constantly
