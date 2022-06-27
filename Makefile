.PHONY: frontend backend controller build

service ?= all

## help: show available command and description
help:
	@echo 'Usage:'
	@sed -n 's/^##//p' ${MAKEFILE_LIST} | column -t -s ':' | sed  -e 's/^/ /'

## assembleDebug: build the app
assembleDebug:
	cd CallReceiver; ./gradlew assembleDebug

## installDebug: deploy the app to specific device
installDebug:
	cd CallReceiver; ./gradlew installDebug
	adb shell am start -a android.intent.action.MAIN -n com.htchan.callreceiver/com.htchan.callreceiver.MainActivity

## emulator: start specific emulator
emulator:
	emulator -avd Pixel_5_API_32 &
