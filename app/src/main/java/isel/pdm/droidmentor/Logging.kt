package isel.pdm.droidmentor

/** Prefix of every log tag in the app, so all its logs can be filtered at once. */
const val APP_TAG = "DroidMentor"

/** Builds the log tag of [className], e.g. "DroidMentor.ActiveChatScreenViewModel". */
fun buildLogTag(className: String) = "$APP_TAG.$className"
