local Players = game:GetService("Players")
local UserInputService = game:GetService("UserInputService")
local RunService = game:GetService("RunService")
local TweenService = game:GetService("TweenService")
local player = Players.LocalPlayer
local character = player.Character or player.CharacterAdded:Wait()
local humanoid = character:WaitForChild("Humanoid")

player.CharacterAdded:Connect(function(char)
	character = char
	humanoid = char:WaitForChild("Humanoid")
end)

local screenGui = Instance.new("ScreenGui")
screenGui.Name = "OneClickTeleportGUI"
screenGui.ResetOnSpawn = false
screenGui.Parent = player:WaitForChild("PlayerGui")

local mainFrame = Instance.new("Frame")
mainFrame.Name = "DraggableFrame"
mainFrame.Size = UDim2.new(0, 170, 0, 250)
mainFrame.Position = UDim2.new(0, 450, 0, 10)
mainFrame.BackgroundColor3 = Color3.fromRGB(45, 45, 45)
mainFrame.BorderSizePixel = 0
mainFrame.Active = true
mainFrame.Draggable = true
mainFrame.Parent = screenGui

local exitButton = Instance.new("TextButton")
exitButton.Name = "ExitButton"
exitButton.Size = UDim2.new(0, 150, 0, 50)
exitButton.Position = UDim2.new(0, 10, 0, 10)
exitButton.Text = "Exit"
exitButton.BackgroundColor3 = Color3.fromRGB(180, 0, 0)
exitButton.TextColor3 = Color3.new(1, 1, 1)
exitButton.Font = Enum.Font.SourceSansBold
exitButton.TextSize = 20
exitButton.Parent = mainFrame

local teleportButton = Instance.new("TextButton")
teleportButton.Name = "TeleportButton"
teleportButton.Size = UDim2.new(0, 150, 0, 50)
teleportButton.Position = UDim2.new(0, 10, 0, 70)
teleportButton.Text = "Bring Enemies (R)"
teleportButton.BackgroundColor3 = Color3.fromRGB(0, 180, 90)
teleportButton.TextColor3 = Color3.new(1, 1, 1)
teleportButton.Font = Enum.Font.SourceSansBold
teleportButton.TextSize = 20
teleportButton.Parent = mainFrame

local healButton = Instance.new("TextButton")
healButton.Name = "HealButton"
healButton.Size = UDim2.new(0, 150, 0, 50)
healButton.Position = UDim2.new(0, 10, 0, 130)
healButton.Text = "Heal (F)"
healButton.BackgroundColor3 = Color3.fromRGB(200, 100, 100)
healButton.TextColor3 = Color3.new(1, 1, 1)
healButton.Font = Enum.Font.SourceSansBold
healButton.TextSize = 20
healButton.Parent = mainFrame

local speedButton = Instance.new("TextButton")
speedButton.Name = "SpeedButton"
speedButton.Size = UDim2.new(0, 150, 0, 50)
speedButton.Position = UDim2.new(0, 10, 0, 190)
speedButton.Text = "Speed (G): OFF"
speedButton.BackgroundColor3 = Color3.fromRGB(255, 200, 50)
speedButton.TextColor3 = Color3.new(0, 0, 0)
speedButton.Font = Enum.Font.SourceSansBold
speedButton.TextSize = 20
speedButton.Parent = mainFrame

local dragging
local dragInput
local dragStart
local startPos

mainFrame.InputBegan:Connect(function(input)
	if input.UserInputType == Enum.UserInputType.MouseButton1 or input.UserInputType == Enum.UserInputType.Touch then
		dragging = true
		dragStart = input.Position
		startPos = mainFrame.Position
		input.Changed:Connect(function()
			if input.UserInputState == Enum.UserInputState.End then
				dragging = false
			end
		end)
	end
end)

mainFrame.InputChanged:Connect(function(input)
	if input.UserInputType == Enum.UserInputType.MouseMovement or input.UserInputType == Enum.UserInputType.Touch then
		dragInput = input
	end
end)

UserInputService.InputChanged:Connect(function(input)
	if input == dragInput and dragging then
		local newPos = input.Position - dragStart
		mainFrame.Position = UDim2.new(startPos.X.Scale, startPos.X.Offset + newPos.X, startPos.Y.Scale, startPos.Y.Offset + newPos.Y)
	end
end)

local offset = Vector3.new(0, 0, -5)
local maxDistance = 80
local speedEnabled = false

local function isNPC(model)
	if not model:IsA("Model") then return false end
	if Players:GetPlayerFromCharacter(model) then return false end
	return model:FindFirstChild("Humanoid") and model:FindFirstChild("HumanoidRootPart")
end

local function teleportNearbyNPCs()
	local char = player.Character
	if not char then return end

	local myHRP = char:FindFirstChild("HumanoidRootPart")
	if not myHRP then return end

	local nearbyNPCs = {}
	for _, obj in ipairs(workspace:GetDescendants()) do
		if obj:IsA("Part") and obj.Name == "HumanoidRootPart" then
			local npc = obj.Parent
			if isNPC(npc) and (obj.Position - myHRP.Position).Magnitude <= maxDistance then
				table.insert(nearbyNPCs, npc)
			end
		end
	end

	for _, npc in ipairs(nearbyNPCs) do
		local enemyHRP = npc:FindFirstChild("HumanoidRootPart")
		local enemyHumanoid = npc:FindFirstChild("Humanoid")

		if enemyHRP and enemyHumanoid and enemyHumanoid.Health > 0 then
			enemyHRP.CFrame = CFrame.new(5, 5, 5)

			local tweenInfo = TweenInfo.new(0.5)
			local targetCFrame = enemyHRP.CFrame * CFrame.new(0, 0, 5)
			local tween = TweenService:Create(myHRP, tweenInfo, {CFrame = targetCFrame})
			tween:Play()
			tween.Completed:Wait()

			while enemyHumanoid and enemyHumanoid.Parent and enemyHumanoid.Health > 0 do
				firesignal(game:GetService("Players").LocalPlayer.PlayerGui.Mobile.Tablet.AttackTablet.Activated)
				task.wait(0.8)
			end
		end
	end
end

local function findNearestSeat()
	local char = player.Character
	if not char then return nil end
	local hrp = char:FindFirstChild("HumanoidRootPart")
	if not hrp then return nil end

	local nearest = nil
	local shortestDist = math.huge

	for _, seat in ipairs(workspace:GetDescendants()) do
		if seat:IsA("Seat") and not seat.Disabled then
			local dist = (seat.Position - hrp.Position).Magnitude
			if dist < shortestDist then
				shortestDist = dist
				nearest = seat
			end
		end
	end
	return nearest
end

local function healPlayer()
	local char = player.Character
	if not char then return end
	local hrp = char:FindFirstChild("HumanoidRootPart")
	local humanoid = char:FindFirstChild("Humanoid")
	if not hrp or not humanoid then return end

	local seat = findNearestSeat()
	if not seat then
		warn("No usable seat found!")
		return
	end

	local originalCFrame = hrp.CFrame

	local wasAnchored = seat.Anchored
	seat.Anchored = true

	hrp.CFrame = seat.CFrame + Vector3.new(0, 2, 0)

	while true do
		task.wait(1)
		local hpLabel = player:FindFirstChild("PlayerGui")
			and player.PlayerGui:FindFirstChild("Main")
			and player.PlayerGui.Main:FindFirstChild("HomePage")
			and player.PlayerGui.Main.HomePage:FindFirstChild("Bottom")
			and player.PlayerGui.Main.HomePage.Bottom:FindFirstChild("Main")
			and player.PlayerGui.Main.HomePage.Bottom.Main:FindFirstChild("Health")
			and player.PlayerGui.Main.HomePage.Bottom.Main.Health:FindFirstChild("Num")
			and player.PlayerGui.Main.HomePage.Bottom.Main.Health.Num:FindFirstChild("Health")

		if hpLabel and hpLabel:IsA("TextLabel") then
			local text = hpLabel.Text
			local current, max = string.match(text, "(%d+)%s*/%s*(%d+)")
			if current and max and tonumber(current) == tonumber(max) then
				break
			end
		else
			warn("HP label not found!")
			break
		end
	end

	humanoid.Sit = false
	task.wait(0.1)

	hrp.CFrame = originalCFrame

	seat.Anchored = wasAnchored
end

UserInputService.JumpRequest:Connect(function()
	if humanoid then
		humanoid:ChangeState(Enum.HumanoidStateType.Jumping)
	end
end)

RunService.Stepped:Connect(function()
	if speedEnabled and humanoid then
		humanoid.WalkSpeed = 60
	end
end)

local function toggleSpeed()
	speedEnabled = not speedEnabled
	speedButton.Text = "Speed (G): " .. (speedEnabled and "ON" or "OFF")
	if not speedEnabled and humanoid then
		humanoid.WalkSpeed = 16
	end
end

teleportButton.MouseButton1Click:Connect(teleportNearbyNPCs)
healButton.MouseButton1Click:Connect(healPlayer)
speedButton.MouseButton1Click:Connect(toggleSpeed)

UserInputService.InputBegan:Connect(function(input, gameProcessed)
	if gameProcessed then return end
	if input.KeyCode == Enum.KeyCode.R then
		teleportNearbyNPCs()
	elseif input.KeyCode == Enum.KeyCode.F then
		healPlayer()
	elseif input.KeyCode == Enum.KeyCode.G then
		toggleSpeed()
	end
end)

exitButton.MouseButton1Click:Connect(function()
	screenGui:Destroy()
end)
