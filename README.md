# NFCPassword
A small android application to store a (master) password encrypted on a NFC tag.

## Prerequisites

* Android Studio
* Android SDK API Level 26 (Android 8.0 Oreo)
* a NFC tag (for instance NTAG203 or NTAG213)

## How to use the app?
The app does two things: store a password onto a NFC tag or read it from NFC tag into clipboard. 

### Store your password on the tag
* input your password
* press upload button
* hold NFC tag close

### Read your password from the tag
* hold NFC tag close
* password is decrypted and put into clipboard
* use your password
* dont forget to clear the clipboard after usage ([Clipboard Safety](https://www.reddit.com/r/Bitwarden/comments/cwinrh/is_my_password_safe_while_copied_to_clipboard_in/))

## What key is used to encrypt my password?
The Android ID (Settings.Secure.ANDROID_ID or SSAID) see:

[Device identifier](https://android-developers.googleblog.com/2017/04/changes-to-device-identifiers-in.html)

Backup your password before doing a factory reset!

## Is it safe?
Most likely not. 

Use it without warranty and at your own risk.
