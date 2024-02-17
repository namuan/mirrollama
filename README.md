<p align="center">
  <img src="https://github.com/namuan/mirrollama/raw/main/docs/images/app-logo.png" width="128px"/>
</p>
<h1 align="center">MirrOllama :: Multiplying the Power of Ollama models</h1>  

#### Features

![MirrOllama](docs/images/mirrollama-screenshot.png)

#### Development

You'll need Java17 and JavaFX to build and run this application.
The easiest way is to use [SDKMAN](https://sdkman.io/).

Once you have SDKMAN installed, you can install `17.0.5-tem`.

```shell
sdk install java 17.0.5-tem
```

Once it is installed, just run the following command to build the application.

```shell
make run 
```  

You can just run `make` to display list of available commands.

```shell
make
```

### Packaging

```shell
make install
```

### Icons

Requires Inkscape and ImageMagick.

```shell
brew install imagemagick
brew install cask inkscape
```

Run the following command to generate icons for all platforms.

```shell
make icons
```
