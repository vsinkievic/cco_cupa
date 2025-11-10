# VSCode/Cursor + DevTools + MapStruct Setup

This configuration enables automatic recompilation when Java files change, making Spring Boot DevTools work seamlessly with MapStruct annotation processors.

## How It Works

1. **Save a Java file** → VSCode/Cursor detects the change
2. **Maven compile runs** → Annotation processors (MapStruct, Lombok) regenerate implementations
3. **DevTools detects new .class files** → Application restarts automatically
4. **Spring finds mapper beans** → Application starts successfully ✅

## Setup Instructions

### 1. Install Required Extension

Open VSCode/Cursor and install:
- **Run on Save** by `emeraldwalk.runonsave`

Or run:
```bash
code --install-extension emeraldwalk.runonsave
```

### 2. Configuration (Already Done)

The following files are already configured:
- `.vscode/settings.json` - Enables auto-compile on save
- `.vscode/tasks.json` - Defines Maven tasks
- `.vscode/extensions.json` - Recommends required extensions

### 3. Verify Setup

1. Start the application:
   ```bash
   ./mvnw spring-boot:run
   ```

2. Open any Java file (e.g., a service or controller)

3. Make a small change (add a comment)

4. Save the file (Ctrl+S)

5. Watch the bottom status bar - you should see Maven compiling

6. DevTools should auto-restart the application

## Troubleshooting

### Problem: Application doesn't restart after saving

**Solution**: Check if "Run on Save" extension is installed and enabled.

### Problem: Maven compile is too slow

**Solution**: The compile is incremental, so only changed files recompile. First compile after changes is slower.

### Problem: MapStruct implementations not found

**Solution**: Run full clean compile:
```bash
./mvnw clean compile
```

## Alternative: Manual Compile (Fallback)

If automatic compilation isn't working, you can manually trigger compile:

1. Press `Ctrl+Shift+B` (Build task)
2. Or run in terminal: `./mvnw compile`

## When to Do Full Rebuild

Run `./mvnw clean compile` when:
- Adding new mapper interfaces
- Changing mapper method signatures
- After pulling code changes
- When you get NoClassDefFoundError

## Performance Tips

- **Exclude target/** from file watchers to avoid double-compilation
- **Use `-q` flag** (quiet mode) - already configured
- **Close unnecessary files** - reduces load on Java language server

## Status

✅ DevTools: **ENABLED** (automatic restart)  
✅ LiveReload: **ENABLED** (frontend auto-refresh)  
✅ Auto-compile: **ENABLED** (on .java file save)  
✅ MapStruct: **WORKING** (annotation processors run on compile)

---

**Need help?** Check the VSCode Output panel → "Run on Save" for compilation logs.


