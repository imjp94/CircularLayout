# CircularLayout

Layout children views in circle

## Behaviours

### XML Attributes

View.LayoutParam

- width/height:
  - MATCH_PARENT: Only works for root layout or layout with center as true, use maximum parent's space or parent CircularLayout's center space as radius
  - WRAP_CONTENT: Minimum radius to fit its own content
  - Specific: The largest value from width/height will be used as radius

CircularLayout

- offset_angle: Angle to move from origin position
- LayoutParam
  - center(boolean): true for positioning it to center, default false
  - rotate(boolean): true for calling setRotation according to angle relative to center, default false

## Usage

Declare namespace for using CircularLayout's custom attributes

``` xml
xmlns:app="http://schemas.android.com/apk/res-auto"
```

``` xml
<com.imjp.circularlayout.CircularLayout
    android:layout_width="wrap_content"
    android:layout_height="wrap_content">

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="1"
        app:rotate="true" />

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="2"
        app:rotate="true" />

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="3"
        app:rotate="true" />

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="4"
        app:rotate="true" />

</com.imjp.circularlayout.CircularLayout>
```

## License

[MIT License](LICENSE)