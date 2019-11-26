# PokeLeaks: Gotta Catch 'Em All 🐤🔍

For this workshop, we will work with a modified version of
[CatchUp](https://github.com/ZacSweers/CatchUp/), an open source app from
[Zac Sweers](https://github.com/ZacSweers). The app has been modified to introduce memory leaks,
and you have one job: fix all the leaks!

## Download Android Studio 4.0 Canary 4

CatchUp requires the latest Android Studio **Canary**.
Go [here](https://developer.android.com/studio/preview) and click on **DOWNLOAD 4.0 CANARY 4**.

## Download the source code

Download a zip of the source code [here](https://github.com/pyricau/CatchLeaks/archive/master.zip)
or check it out:

```
git clone https://github.com/pyricau/CatchLeaks.git
```

## Build & Run

Open the project in Android Studio Canary, and then build and run the app. You can deploy to an
emulator or device with an SDK version greater than 21 (Android 5 / Lollipop).

You should see a screen with several tabs, one for each news service.

## Profile the app

In Android Studio, click on **Profiler** at the bottom, then **+**, select your device and select
the `io.sweers.catchup.debug` process. Once it is done loading, click on the Memory graph.

## Observe a memory leak

Rotate the screen a few times. If you're on an emulator, you can rotate back and forth with
`Cmd + left arrow / Cmd + right arrow`.

As you rotate the screen, notice how the memory is increasing, even if you force the garbage collector
to run (by clicking on the trash icon in the memory profiler). There's a memory leak!

## Add LeakCanary

Follow the instructions [here](https://square.github.io/leakcanary/getting_started/).

Now is a great time to take a look at the updated [Fundamentals](https://square.github.io/leakcanary/fundamentals/)
section and take a refresher on memory leaks!

## Reproduce the leak

Try rotating the screen again. This time, notice the "retained object" notification. Can you
figure out what to do next?

## Investigate and fix

LeakCanary should give you enough information to identify the source of the leak and fix it. If
you're thinking of taking a random guess or are not sure how to proceed next, make sure you've
read the [Fundamentals](https://square.github.io/leakcanary/fundamentals/) then raise your hand,
and we'll go through it together.

Before you fix the leak: notice how if you rotate the screen N times, the notification will say
"N retained objects", however after the analysis LeakCanary will report only one leak. Why is that?

Fix the leak, and confirm that it isn't happening any more.

Once you're done, offer help to one of your neighbors. Teaching is a great way to learn! Let's
try to all be done with this first leak before we individually move on to the next one.

## New Leak: fragmented

Go the the Y tab (Hacker News), then go to the basketball tab (Dribble), then wait a bit. You
should see a leak notification.

What's different about this leak?

## New leak: settings

Tap on the wheel in the top right corner and rotate the screen a few times.

Once you've found and fix this leak: can you think of ways to prevent this type of leaks from
being introduced? Maybe notice how there's already something in place for that in that codebase.

## New leak: a classic on Stack Overflow

Go the settings, the click on **About** and then **Learn More**

This one is a common mistake, often reported in
[StackOverflow #leakcanary](https://stackoverflow.com/questions/tagged/leakcanary?sort=active)
questions. It is far from obvious, and the mistake is not well documented. Good luck!

## Done?

Are you done? Don't worry there's plenty more you can do!

* Help one of your neighbors
* Update your app to [LeakCanary 2 Beta 5](https://square.github.io/leakcanary/getting_started/)
* Ask for feedback on some leaks in your own app
* Read the [documentation](https://square.github.io/leakcanary) and highlight confusing sections
or missing details. You can open up a new issue or bring it up in person!