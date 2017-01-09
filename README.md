# iotcontroller-android
This app can send data to a Medium One project and visualize the data through many widgets.

### Features
- Build dashboards to visualize your device data
- Send GPS data to your Medium One project
- Send triggers for your Medium One workflows
- Receive push notifications from your Medium One workflows

## Installation
After cloning this repo:

1. Pull in the `volley` dependency

    `git submodule init && git submodule sync && git submodule update --remote`

1. Add your `google-services.json` credentials file

    `cp google-services.json iotcontroller-android/app/`

1. Create `app/src/main/res/values/config.xml` to provide your Google Maps API Key

    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <resources>
        <string name="google_maps_api_key">YOUR_KEY_HERE</string>
    </resources>
    ```

1. Ensure you have the necessary SDK versions to build the `volley` dependency and the iotcontroller-android app
