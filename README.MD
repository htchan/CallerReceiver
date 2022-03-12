# Call Receiver

This App help you identify who is calling you.

# Logic Flow
1. Get the phone number calling you
2. Query contact list to ensure the call is from someone you don't know
3. Send Https request to [HK Junk Call](https://hkjunkcall.com/) to find out who is calling you
4. Make a toast to tell you who is calling you

# Permission Request
- READ_PHONE_STATE: check phone status (ringing) and read phone number (incoming call)
- READ_CONTACT: check if the incoming call number exist in contact list or not
- INTERNET: send request to find out who is calling you

# Disclaimer
- This is just a personal project, use this by your own risk and developer will not bear any responsibility for any use it in any means

# Future works
- add notification option for user to choose showing toast or notification