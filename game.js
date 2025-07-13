// Game variables
const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');
const scoreElement = document.getElementById('score');
const livesElement = document.getElementById('lives');
const timeElement = document.getElementById('time');
const startScreen = document.getElementById('startScreen');
const gameOverScreen = document.getElementById('gameOver');
const finalScoreElement = document.getElementById('finalScore');
const finalTimeElement = document.getElementById('finalTime');

let gameRunning = false;
let score = 0;
let lives = 3;
let gameTime = 0;
let lastTime = 0;

// Game objects
let missiles = [];
let lasers = [];
let buildings = [];
let explosions = [];

// Mouse position
let mouseX = 0;
let mouseY = 0;

// Game settings
const MISSILE_SPEED_MIN = 2;
const MISSILE_SPEED_MAX = 5;
const LASER_SPEED = 8;
const MISSILE_SPAWN_RATE = 60; // frames between spawns
let missileSpawnCounter = 0;

// Initialize buildings
function initBuildings() {
    buildings = [];
    const buildingCount = 8;
    const buildingWidth = 60;
    const spacing = (canvas.width - buildingCount * buildingWidth) / (buildingCount + 1);
    
    for (let i = 0; i < buildingCount; i++) {
        buildings.push({
            x: spacing + i * (buildingWidth + spacing),
            y: canvas.height - 100,
            width: buildingWidth,
            height: 80 + Math.random() * 40,
            color: `hsl(${200 + Math.random() * 60}, 70%, 50%)`
        });
    }
}

// Create missile
function createMissile() {
    const side = Math.random() < 0.5 ? 'left' : 'right';
    const x = side === 'left' ? -20 : canvas.width + 20;
    const y = Math.random() * 200 + 50;
    const speed = MISSILE_SPEED_MIN + Math.random() * (MISSILE_SPEED_MAX - MISSILE_SPEED_MIN);
    
    missiles.push({
        x: x,
        y: y,
        vx: side === 'left' ? speed : -speed,
        vy: speed * 0.5 + Math.random() * speed * 0.5,
        width: 15,
        height: 8,
        side: side
    });
}

// Create laser
function createLaser(x, y) {
    lasers.push({
        x: x,
        y: y,
        vx: 0,
        vy: -LASER_SPEED,
        width: 3,
        height: 15
    });
}

// Create explosion
function createExplosion(x, y) {
    explosions.push({
        x: x,
        y: y,
        radius: 0,
        maxRadius: 30,
        alpha: 1
    });
}

// Check collision between two rectangles
function checkCollision(rect1, rect2) {
    return rect1.x < rect2.x + rect2.width &&
           rect1.x + rect1.width > rect2.x &&
           rect1.y < rect2.y + rect2.height &&
           rect1.y + rect1.height > rect2.y;
}

// Update game objects
function update(deltaTime) {
    if (!gameRunning) return;
    
    // Update game time
    gameTime += deltaTime;
    timeElement.textContent = Math.floor(gameTime / 1000);
    
    // Spawn missiles
    missileSpawnCounter++;
    const spawnRate = Math.max(20, MISSILE_SPAWN_RATE - Math.floor(gameTime / 10000));
    if (missileSpawnCounter >= spawnRate) {
        createMissile();
        missileSpawnCounter = 0;
    }
    
    // Update missiles
    for (let i = missiles.length - 1; i >= 0; i--) {
        const missile = missiles[i];
        missile.x += missile.vx;
        missile.y += missile.vy;
        
        // Check if missile hit building
        for (let j = buildings.length - 1; j >= 0; j--) {
            const building = buildings[j];
            if (checkCollision(missile, building)) {
                missiles.splice(i, 1);
                buildings.splice(j, 1);
                lives--;
                livesElement.textContent = lives;
                createExplosion(missile.x, missile.y);
                
                if (lives <= 0) {
                    gameOver();
                }
                break;
            }
        }
        
        // Remove missiles that are off screen
        if (missile.x < -50 || missile.x > canvas.width + 50 || missile.y > canvas.height + 50) {
            missiles.splice(i, 1);
        }
    }
    
    // Update lasers
    for (let i = lasers.length - 1; i >= 0; i--) {
        const laser = lasers[i];
        laser.x += laser.vx;
        laser.y += laser.vy;
        
        // Check laser-missile collisions
        for (let j = missiles.length - 1; j >= 0; j--) {
            const missile = missiles[j];
            if (checkCollision(laser, missile)) {
                lasers.splice(i, 1);
                missiles.splice(j, 1);
                score++;
                scoreElement.textContent = score;
                createExplosion(missile.x, missile.y);
                break;
            }
        }
        
        // Remove lasers that are off screen
        if (laser.y < -20) {
            lasers.splice(i, 1);
        }
    }
    
    // Update explosions
    for (let i = explosions.length - 1; i >= 0; i--) {
        const explosion = explosions[i];
        explosion.radius += 2;
        explosion.alpha -= 0.05;
        
        if (explosion.alpha <= 0) {
            explosions.splice(i, 1);
        }
    }
}

// Draw game objects
function draw() {
    // Clear canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // Draw sky gradient
    const gradient = ctx.createLinearGradient(0, 0, 0, canvas.height);
    gradient.addColorStop(0, '#87CEEB');
    gradient.addColorStop(1, '#98FB98');
    ctx.fillStyle = gradient;
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    // Draw buildings
    buildings.forEach(building => {
        ctx.fillStyle = building.color;
        ctx.fillRect(building.x, building.y, building.width, building.height);
        
        // Draw windows
        ctx.fillStyle = '#FFD700';
        const windowSize = 8;
        const windowSpacing = 15;
        for (let x = building.x + 10; x < building.x + building.width - 10; x += windowSpacing) {
            for (let y = building.y + 10; y < building.y + building.height - 10; y += windowSpacing) {
                if (Math.random() > 0.3) { // Some windows are lit
                    ctx.fillRect(x, y, windowSize, windowSize);
                }
            }
        }
    });
    
    // Draw missiles
    missiles.forEach(missile => {
        ctx.fillStyle = '#FF4444';
        ctx.fillRect(missile.x, missile.y, missile.width, missile.height);
        
        // Draw missile trail
        ctx.strokeStyle = '#FF8888';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(missile.x + missile.width/2, missile.y + missile.height/2);
        ctx.lineTo(missile.x + missile.width/2 + missile.vx * 3, missile.y + missile.height/2 + missile.vy * 3);
        ctx.stroke();
    });
    
    // Draw lasers
    lasers.forEach(laser => {
        ctx.fillStyle = '#00FFFF';
        ctx.fillRect(laser.x, laser.y, laser.width, laser.height);
        
        // Draw laser glow
        ctx.shadowColor = '#00FFFF';
        ctx.shadowBlur = 10;
        ctx.fillRect(laser.x, laser.y, laser.width, laser.height);
        ctx.shadowBlur = 0;
    });
    
    // Draw explosions
    explosions.forEach(explosion => {
        ctx.globalAlpha = explosion.alpha;
        ctx.fillStyle = '#FFAA00';
        ctx.beginPath();
        ctx.arc(explosion.x, explosion.y, explosion.radius, 0, Math.PI * 2);
        ctx.fill();
        
        ctx.fillStyle = '#FF4444';
        ctx.beginPath();
        ctx.arc(explosion.x, explosion.y, explosion.radius * 0.7, 0, Math.PI * 2);
        ctx.fill();
        
        ctx.globalAlpha = 1;
    });
    
    // Draw defense system (crosshair)
    if (gameRunning) {
        ctx.strokeStyle = '#00FFFF';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(mouseX - 15, mouseY);
        ctx.lineTo(mouseX + 15, mouseY);
        ctx.moveTo(mouseX, mouseY - 15);
        ctx.lineTo(mouseX, mouseY + 15);
        ctx.stroke();
        
        ctx.beginPath();
        ctx.arc(mouseX, mouseY, 20, 0, Math.PI * 2);
        ctx.stroke();
    }
}

// Game loop
function gameLoop(timestamp) {
    const deltaTime = timestamp - lastTime;
    lastTime = timestamp;
    
    update(deltaTime);
    draw();
    
    requestAnimationFrame(gameLoop);
}

// Mouse event handlers
canvas.addEventListener('mousemove', (e) => {
    const rect = canvas.getBoundingClientRect();
    mouseX = e.clientX - rect.left;
    mouseY = e.clientY - rect.top;
});

canvas.addEventListener('click', () => {
    if (gameRunning) {
        createLaser(mouseX, mouseY);
    }
});

// Game control functions
function startGame() {
    gameRunning = true;
    score = 0;
    lives = 3;
    gameTime = 0;
    missiles = [];
    lasers = [];
    explosions = [];
    missileSpawnCounter = 0;
    
    scoreElement.textContent = score;
    livesElement.textContent = lives;
    timeElement.textContent = '0';
    
    startScreen.style.display = 'none';
    gameOverScreen.style.display = 'none';
    
    initBuildings();
}

function gameOver() {
    gameRunning = false;
    finalScoreElement.textContent = score;
    finalTimeElement.textContent = Math.floor(gameTime / 1000);
    gameOverScreen.style.display = 'block';
}

function restartGame() {
    startGame();
}

// Start the game loop
requestAnimationFrame(gameLoop);