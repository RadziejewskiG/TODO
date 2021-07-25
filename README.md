# TODO App

### The main goals
The main focus of this project is to showcase clean and modern MVVM architecture implementation with the usage of core tools and technologies used in professional Android development. It has:
- dependency injection set up properly,
- navigation between destinations using navigation graphs,
- heavy work running on a proper background threads,
- it follows the Clean Code principles,
- live updates with the usage of the Firebase Firestore database

and more.

### About the app
This is a simple To Do app where users can add/edit tasks with a title, description and icon url. The items are sorted by creation date and can be checked as completed. The data is stored in the Firebase Firestore database and Firebase Storage (photos). The task list is paginated and updated in real time when a change in the collection occurs, even outside of the app. Next page is loaded when the user reaches the end of the current list and when an error occurs, the proper error message is shown on the screen.

### Screens and features
- Task list screen with a refresh button and a fab redirecting to item creation form. Clicking on an item redirects to the item edition form. The tasks can be deleted by long pressing on them.
- Task edit form screen: you can edit the task including taking/selecting an existing photo as an icon for your task.
- Take a photo screen: you can take a photo, rotate it and crop it in order to use it as an icon for your task. All taken photos are saved in the Firebase Storage and can be used later.
- Select a photo screen: it contains a list of all the photos that are saved in the Firebase Storage. You can select a photo in order to set it as your task&apos;s icon.

### Tech stack
- Kotlin
- MVVM
- Dagger 2
- Coroutines, Flow
- Firebase platform: Firestore, Storage
- Glide
- Architecture Components
- Navigation Component
- Material design
- Material Components

### How to set up with the Firebase platform
1. Change the project&apos;s package name (the existing one is already registered) - remember to change the file provider url too (in CAPTURE_PHOTO_FILE_PROVIDER_URL constant and in the manifest).
2. Navigate to https://console.firebase.google.com/ and create a new project - just follow the steps.
3. Enable the Firebase Firestore and the Firebase Storage in the console.
4. Add the following composite index in the Cloud Firestore:
Collection ID: tasks
Fields indexed: completed Ascending, createdAt Descending, title Ascending
Query scope: Collection
5. Edit the rules for the Firestore and Storage allowing reading and writing for unauthenticated users - there is no authentication for now.
