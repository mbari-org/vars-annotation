# Keymap

![MBARI logo](../images/mbari-logo.png)

## Current Keymap

At MBARI, many video control keys are mapped to a [Shuttle Pro](https://www.contourdesign.com/product/shuttle/) profile, giving users ergonomic control over video playback.

### Video Actions using Home Row

```text
Frame advance                   [cmd]+I
Shuttle Reverse (0.06)          [cmd]+G
Shuttle Reverse (0.03)          [cmd]+J
Toggle Play/Stop                [cmd]+K
Shuttle Forward (0.03)          [cmd]+L
Shuttle Forward (0.06)          [cmd]+;
Seek back 1/10 time jump        [cmd]+M
Seek back 1 time jump           [cmd]+,
Seek forward 1 time jump        [cmd]+.
```

The _time jump_ defaults to 1 second (so [cmd]+M seeks back 0.1 second). You can change it with **Time jump for key commands** in the Sharktopoda settings.

### Actions using Arrows

```text
Frame advance                   [cmd]+up
Seek back 1 time jump           [cmd]+left
Toggle Play/Stop                [cmd]+down
Shuttle Forward (0.03)          [cmd]+right
```

### Other

```text
Toggle Play/Stop                [ctrl]+[space]
Select previous row             [cmd]+[shift]+up
Select next row                 [cmd]+[shift]+down
New annotation                  [cmd]+N
Copy annotation to new time     [cmd]+R
Copy annotation with frame-grab [cmd]+[shift]+R
Duplicate annotation            [cmd]+T
Frame capture                   [cmd]+F
Delete annotations              [cmd]+[delete]
Undo                            [cmd]+Z
Redo                            [cmd]+[shift]+Z
Seek to selected annotation     [cmd]+[shift]+S
```

**On Windows and Linux, substitute `ctrl` for `cmd` in the shortcuts above.**
