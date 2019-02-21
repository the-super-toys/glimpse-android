# WIP

# Glimpse
A content-aware cropping library for Android

Give the right first impression with just a glimpse. Instead of center cropping images blindly leverage Glimpse's eye to catch the right region.

## Setup
Add to top level *gradle.build* file
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Add to app module *gradle.build* file
```gradle
dependencies {
	implementation 'com.github.thesupertoys:glimpse-core:0.0.1'
	
	//Glide extensions for glimpse
	implementation 'com.github.thesupertoys:glimpse-glide:0.0.1'
}
```

## Usage

### Init Glimpse in your app base class

```kotlin
class YourApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Glimpse.init(this)
    }
}
```

### Use Glimpse without extensions
First compute image's focal points by calling `Bitmap.findCenter` extension function.

```kotlin
val original = (ivOriginal.drawable as BitmapDrawable).bitmap
val (x, y) = original.findCenter()
```

Then supply those focal points alongside with the width and height values of the target `ImageView` to `Bitmap.crop` extension function.

```kotlin
val cropped = original.crop(x, y, imageView.layoutParams.width, imageView.layoutParams.height)
imageView.setImageBitmap(cropped)
```

### Use Glimpse leveraging Glide extension 

### What about other image loaders such as Picasso and Fresco?
We tried to ship Glimpse with Picasso and Fresco extensions but we were not able to find the right way to do it.
Explain it more     

## Sample app
`:sample_app` module showcase Glimpse usage by making use of Glide extensions.

## Disclaimer

### Runtime cost
Is glimpse ready for production? It may be, let us know! 

Ideally you should not use Glimpse to crop the same image over and over. Even if you use Glide extension which caches the output transformation, the underlying calculation 
will be performed in every android client. Thus, if possible send to your cloud solution the x and y focal points of the calculated crop alongside the image when user uploads content.

We timed the crop calculation on an OnePlus 6 and in average it takes 60 mm. We'll try to improve it but at the time being that's the mark. 


### Compile cost