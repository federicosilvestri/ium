open System
#load "lwc.fsx"

open System.Drawing
open System.Windows.Forms

open Lwc

type Spaceship() as this = 
    inherit LWControl()

    let mutable x : single = 0.f
    let mutable y : single = 0.f
    let scalaF = 1.8f
    let defaultSize = SizeF(12.5f, 12.5f * scalaF)
    let mutable size = defaultSize
    let mutable transform = W2V()
    let mutable xSpeed : single = 0.0f
    let mutable ySpeed : single = 0.0f
    let mutable currentRotation : double = 0.0
    let angConv : double = Math.PI / 180.0
    let acceleration = 0.5
    let maxSpeed = 4.5f
    let maxDistance = 50
    let mutable minDistancePlanets = 0
    let landingDist = 10

    let baricenterPoint () =
        let w2 = single size.Width / single 2
        let h2 = single size.Height / single 3

        PointF(single x + w2, single y + h2)

    let computeSize () =
        let s = if minDistancePlanets < 0 then
                    defaultSize.Width
                elif minDistancePlanets > maxDistance then
                    defaultSize.Width
                else
                    let f = single minDistancePlanets
                    f * defaultSize.Width / single maxDistance
        size <- SizeF(s, s * scalaF)

        if minDistancePlanets < landingDist then
            xSpeed <- 0.f
            ySpeed <- 0.f

    member this.MinDistance with get() = minDistancePlanets
                            and set(v) = (
                                minDistancePlanets <- v
                                computeSize()
                            )
    member this.Size with   get() = size
    member this.Transform with  get() = transform
    member this.W2V with        get() = transform.W2V
    member this.V2W with        get() = transform.V2W
    member this.XSpeed with     get() = xSpeed and set(v : single) = xSpeed <- xSpeed + v
    member this.YSpeed with     get() = ySpeed and set(v : single) = ySpeed <- ySpeed + v
    member this.Location with   get() = PointF(x, y)
    member this.SetLocation (x0 : single, y0 : single) = 
        let w2 = single size.Width / 2.f
        let h2 = single size.Height / 3.f
        
        x <- x0 - w2
        y <- y0 - h2

    override this.HitTest (p : PointF) = 
        let ptran = TransformP this.V2W p
        let point = PointF(single x, single y)
        let r = RectangleF(point, this.Size) in r.Contains(ptran)

    member this.Paint (g : Graphics) =
        let h = single this.Size.Height
        let w = single this.Size.Width
        let w2 = single w / single 2
        
        let aux = this.Transform.W2V.Clone();
        aux.Multiply(g.Transform);
        aux.RotateAt(single currentRotation, baricenterPoint())
        g.Transform <- aux;

        // smoothing? yeah
        g.SmoothingMode <- Drawing2D.SmoothingMode.AntiAlias

        g.DrawLine(Pens.Cyan, PointF(x, y), PointF(x + w, y))
        g.DrawLine(Pens.Cyan, PointF(x, y), PointF(x + w2, y + h))
        g.DrawLine(Pens.Cyan, PointF(x + w, y), PointF(x + w2, y + h))
    
    member this.GoLeft () =
        // updated the rotation
        currentRotation <- currentRotation + 15.0
        
    member this.GoRight () =
        // update the rotation
        currentRotation <- currentRotation - 15.0

    member this.Accelerate (acc, angle) = 
        // my function for acceleration
        let xIncr = -Math.Sin(angle * angConv) * acc
        let yIncr = Math.Cos(angle * angConv) * acc

        xSpeed <- xSpeed + single xIncr
        ySpeed <- ySpeed + single yIncr

        let convSpeed x = 
            if x < 0.f then -1.f
            else 1.f

        if Math.Abs(xSpeed) > maxSpeed then 
            xSpeed <- maxSpeed * convSpeed(xSpeed)

        if Math.Abs(ySpeed) > maxSpeed then
            ySpeed <- maxSpeed * convSpeed(ySpeed)

    member this.GoDown () =
        // this is not a coordinate changing, this is a speed changing
        this.Accelerate(-acceleration, currentRotation)

    member this.GoUp () =
        // this is not a coordinate changing, this is a speed changing
        this.Accelerate(acceleration, currentRotation)

    member this.TimeTick () = 
        // we need to update the current coordinates considering current speed
        x <- x + xSpeed
        y <- y + ySpeed

// declaring all types required
type Planet() as this = 
    inherit LWControl()

    let mutable location = PointF()
    let mutable size = SizeF(25.f, 25.f)
    let mutable selected = false
    let mutable color = Color.Gold
    let mutable associatedImage = null
    let mutable img = ()
    let mutable transform = W2V()

    member this.AssociatedImage with get() = associatedImage
                                and set(v) = associatedImage <- v
    member this.Size with       get() = size   
                                and set(l) = size <- l
    member this.RealLocation with   get() = PointF(location.X + size.Width / 2.f, location.Y + size.Height / 2.f)
                                and set(l : PointF) = location <- l
    member this.Location with   get() = location
                                and set(l : PointF) = location <- l
    member this.Selected with   get() = selected
                                and set(b) = selected <- b
    member this.Color with      get() = color
    member this.Transform with  get() = transform
    member this.W2V with        get() = transform.W2V
    member this.V2W with        get() = transform.V2W

    override this.HitTest (p : PointF) = 
        let pW = TransformP this.V2W p 
        let r = RectangleF(this.Location, this.Size) in r.Contains(pW)

    member this.ChangeSize (i : int) =
        let oldSize = this.Size.Height
        if i > 0 then
            this.Size <- SizeF(oldSize + 1.0f, oldSize + 1.0f)
        elif i < 0 then
            this.Size <- SizeF(oldSize - 1.0f, oldSize - 1.0f)

    member this.Paint (g : Graphics) =
        if this.Selected then 
            let pen = Pens.Red 
            let r = RectangleF(location, this.Size) |> RectF2Rect
            g.DrawRectangle(pen, r)
        
        let h = int this.Size.Height
        let x = int location.X
        let y = int location.Y
        
        // smoothing? yeah
        g.SmoothingMode <- Drawing2D.SmoothingMode.AntiAlias

        if (isNull associatedImage) then
            g.FillEllipse(Brushes.Goldenrod, x, y, h, h)
        else 
            g.DrawImage(associatedImage, x, y, h, h)

// declaring the main container that represents the space
type MyContainer() as this =
    inherit LWContainer()

    let yControls = 25
    
    let mutable planets = ResizeArray<Planet>()
    let mutable spaceship = Spaceship()

    let totalStars : int = 50

    let mutable planetPressed = false;
    let mutable nextPlanetPosition = PointF()
    let mutable addingPlanet = false
    let mutable selPlanet : option<Planet> = None 
    
    let mutable draggingPlanet = false
    let mutable dragOffset = PointF()
    let mutable dragStart = PointF()

    let mutable spaceshipPositioned = false
    let mutable spaceshipPositioning = false

    let mutable minDistance = 0.0

    let butSize = 25.f

    // timer for animations
    let mutable timer = new Timer(Interval = 100)

    // working with buttons
    let buttonsText = [|"+";"-";"L";"R";"▲";"▼";"◄";"►";"++";"--";"IMG";|]
    let buttons = ResizeArray<LWCButton>()
    let actions = [|"zoomIn";"zoomOut";"rotateLeft";"rotateRight";"goUp";"goDown";"goLeft";"goRight";"sizeUp";"sizeDown";"img";"setSpaceship"|]
    
    let mutable addSwitch = LWCSwitch(Text = "Add", Position = PointF(butSize * single buttonsText.Length, 0.f), Size = SizeF(butSize, butSize), Parent = this)
    let mutable posSwitch = LWCSwitch(Text = "Pos", Position = PointF(butSize * single (buttonsText.Length + 1), 0.f), Size = SizeF(butSize, butSize), Parent = this)

    let mutable stars = ResizeArray<PointF>()
    let starSize = 2.f

    let generateStars () =
        let rnd = System.Random()
        stars.Clear()
        for i in 0 .. totalStars do
            let xc = rnd.NextDouble() * double this.Size.Width
            let yc = rnd.NextDouble() * double this.Size.Height
            stars.Add(PointF(single xc, single yc))

    // size up
    let sizeChange (x: int) = 
        let p = planets |> Seq.tryFind(fun l -> l.Selected)
        match p with
            | Some pl -> pl.ChangeSize(x)
            | _ -> ()

    let changeImg () =
        let p = planets |> Seq.tryFind(fun l -> l.Selected)
        match p with
            | Some pl ->
                // opening the file dialog box
                let imageStream = this.OpenDialog()
                let image = Image.FromStream(imageStream)
                pl.AssociatedImage <- image
                ()
            | _ -> ()

    let setSpaceShipPosition (x : single, y : single) = 
        spaceshipPositioning <- false
        spaceshipPositioned <- true
        spaceship.SetLocation(x, y)
        this.Invalidate()
        
   // handler for buttons
    let eventHandlerButton (x : String) =
        match x with
            | "goUp" -> this.Transform.Translate(0.f, -10.f);
            | "goDown" -> this.Transform.Translate(0.f, 10.f);
            | "goLeft" -> this.Transform.Translate(-10.f, 0.f);
            | "goRight" -> this.Transform.Translate(10.f, 0.f);
            | "rotateLeft" -> this.Transform.RotateAt(-15.f, PointF(0.f, 0.f))
            | "rotateRight" -> this.Transform.RotateAt(15.f, PointF(0.f, 0.f))
            | "zoomIn" -> 
                this.Transform.Scale(1.1f, 1.1f)
            | "zoomOut" -> 
                this.Transform.Scale(1.f/1.1f, 1.f/1.1f)
            | "sizeUp" -> sizeChange(1)
            | "sizeDown" -> sizeChange(-1)
            | "img" -> changeImg()
            | _ -> ()

    let computeDistances () =
        if planets.Count > 0 then
            // search the planet near spaceship
            // array of ||norm_2||
            let mutable distances = [||]
            // computing distances between spaceship and planets 
            planets |> Seq.iter (fun p ->
                let dX = float (p.RealLocation.X - spaceship.Location.X)
                let dY = float (p.RealLocation.Y - spaceship.Location.Y)
                let ddX = Math.Pow(dX, 2.0)
                let ddY = Math.Pow(dY, 2.0)
                let dP = [|Math.Sqrt(ddX + ddY)|]

                distances <- Array.append dP distances
            )
        
            // now we need to search the minimum
            minDistance <- Array.min distances

            // update spaceship
            spaceship.MinDistance <- int minDistance
        else
            spaceship.MinDistance <- -1

    do
        // generate stars
        generateStars()

        // adding all buttons
        for i in 0 .. (buttonsText.Length - 1) do
            let b = LWCButton(Text = buttonsText.[i], Position = PointF(butSize * single i, 0.f), Size = SizeF(butSize, butSize), Parent = this)
            b.MouseDown.Add(fun _ -> eventHandlerButton(actions.[i]))
            buttons.Add(b)
            this.LWControls.Add(b)

        addSwitch.MouseDown.Add(fun _ -> posSwitch.Pressed <- false)
        posSwitch.MouseDown.Add(fun _ -> addSwitch.Pressed <- false)

        this.LWControls.Add(addSwitch)
        this.LWControls.Add(posSwitch)
       
        // setting background
        this.BackColor <- Color.Black

        // setting the tick
        timer.Tick.Add(
            fun _ -> 
                computeDistances()
                spaceship.TimeTick()
                this.Invalidate()
        )

        // starting timer
        timer.Start()

    member this.MinDistance with get() = minDistance

    member this.OpenDialog () =
        let ofd = new OpenFileDialog()
        ofd.Filter <- "jpg files (*.jpg)|*.jpg";
        ofd.FilterIndex <- 2;
        ofd.RestoreDirectory <- true;

        let re = ofd.ShowDialog()
        ofd.Dispose()

        if (re = DialogResult.OK) then
            // Read the contents of the file into a stream
            ofd.OpenFile();
        else null

    member this.PlanetPressed 
        with get() = planetPressed 
        and set(v) = planetPressed <- v

    override this.OnPaint e =
        let g = e.Graphics

        // saving the matrix (the identity matrix)
        let save = g.Transform

        // painting planets
        planets |> Seq.iter (fun p ->
            let aux = this.Transform.W2V.Clone();
            aux.Multiply(p.W2V);
            g.Transform <- aux;
            p.Paint g
        )

        // painting the spaceship
        if spaceshipPositioned then
            let aux = this.Transform.W2V.Clone()
            aux.Multiply(spaceship.W2V)
            g.Transform <- aux
            spaceship.Paint g

        // drawing components of LWC restoring IdentiyMatrix
        g.Transform <- save

        // smoothing? yeah
        g.SmoothingMode <- Drawing2D.SmoothingMode.AntiAlias

        // drawing deep sky
        stars |> Seq.iter(
            fun point -> 
                let xc = point.X
                let yc = point.Y
                g.FillEllipse(Brushes.White, single xc, single yc, starSize, starSize)
        )

        base.OnPaint e
        
    override this.OnMouseDown e =
        base.OnMouseDown e

        // stopping timer
        timer.Stop()

        // checking if controls are triggered
        if e.Y > yControls then
            // no controls are triggered so
            // now we need to check the insert,drag,select events
            // searching the planet that has a positive hit test
            let myPlanet = planets |> Seq.tryFind(fun l -> l.HitTest (e.Location |> Point2PointF |> TransformP this.Transform.V2W))

            if myPlanet.IsNone then
                // free space! we can add!
                if posSwitch.IsEnabled then
                    // setting ship position
                    let spaceshipPosition = e.Location |> Point2PointF |> TransformP this.Transform.V2W
                    setSpaceShipPosition(spaceshipPosition.X, spaceshipPosition.Y)
                if addSwitch.IsEnabled then
                    nextPlanetPosition <- e.Location |> Point2PointF |> TransformP this.Transform.V2W
                    addingPlanet <- true
            // we have a positive test with a particular planet
            elif e.Button.Equals(MouseButtons.Right) then
                // this is a selection
                // putting all planets selection to off
                planets |> Seq.iter (fun l -> l.Selected <- false)
                // selecting planet
                selPlanet <- None
                match myPlanet with
                    | Some l -> l.Selected <- true; selPlanet <- myPlanet // select a planet
                    | _ -> () // this should be impossibile
                this.Invalidate()
            else
                // the left side is clicked, so we need to start drag and drop operation
                // and a myPlanet is a valid planet
                match myPlanet with
                    |Some l ->  //drag n drop (left click)    
                        dragStart <- l.Location
                        l.Selected <- true
                        selPlanet <- myPlanet
                        draggingPlanet <- true
                        let mutable pV = e.Location |> Point2PointF |> TransformP this.Transform.V2W
                        pV <- pV |> TransformP l.Transform.V2W 
                        dragOffset <- PointF(pV.X - l.Location.X, pV.Y - l.Location.Y) 
                        this.Invalidate()
                    | _ -> () // should be impossible

    override this.OnMouseUp e =
        // set all to unpressed
        buttons |> Seq.iter(fun b -> b.Pressed <- false)

        base.OnMouseUp e

        if addingPlanet then
            let mutable noOne = false
            let n = planets |> Seq.tryFind(fun l -> l.HitTest (e.Location |> Point2PointF |> TransformP this.Transform.V2W))
            match n with
                | Some _ -> noOne <- false
                | None -> noOne <- true
            if noOne then
                nextPlanetPosition <- e.Location |> Point2PointF |> TransformP this.Transform.V2W
                planets.Add(Planet(Location = nextPlanetPosition))
                addingPlanet <- false
                this.Invalidate()
        elif draggingPlanet then
            match selPlanet with
                |Some l -> 
                    draggingPlanet <- false
                    l.Selected <- false
                    selPlanet <- None                 
                    this.Invalidate();
                |None -> ()

        timer.Start()

    override this.OnMouseMove e =
        base.OnMouseMove e
        if draggingPlanet then
            // updating planet dragging coordinates
            match selPlanet with
                | Some l ->
                    let mutable pV = e.Location |> Point2PointF |> TransformP this.Transform.V2W
                    pV <- pV |> TransformP l.Transform.V2W 
                    let newLoc = PointF(pV.X - dragOffset.X, pV.Y - dragOffset.Y)
                    l.Location <- newLoc
                    this.Invalidate();
                | None -> ()

    override this.OnKeyPress key =
        base.OnKeyPress key
        match key.KeyChar with
            | 'M'
            | 'm' -> 
                // planet deletetion
                match selPlanet with
                    | Some l -> 
                        planets.RemoveAll(fun l -> l.Selected) |> ignore
                        this.Invalidate()
                    | None -> ()
            | 'd' -> 
                spaceship.GoLeft()
                this.Invalidate()
            | 'a' -> 
                spaceship.GoRight()
                this.Invalidate()
            | 's' -> 
                spaceship.GoDown()
                this.Invalidate()
            | 'w' ->
                spaceship.GoUp()
                this.Invalidate()
            | _ -> () // do nothing

    override this.OnResize e =
        base.OnResize e
        generateStars()
        ()

    override this.Dispose e = 
        base.Dispose e
        // stopping the animation
        timer.Stop()

// create the main container
let container = new MyContainer(Dock = DockStyle.Fill)

// create the windows form
let form = new Form(Text = "Spaceship Simulator", Size = Size(600,400))

// add the container to form
form.Controls.Add(container)

// finally show the window
form.Show()
