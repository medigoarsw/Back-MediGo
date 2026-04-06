# 🔐 Guía de Registro de Usuarios - MediGo

## ⚠️ Error 400 Resuelto

**Tu error:** Intentaste registrar con `"role": "ADMIN"`

**Causa:** El endpoint `/api/auth/register` **NO permite crear usuarios ADMIN**

---

## ✅ Roles Permitidos para Registro

| Rol | Código | Descripción | Permiso |
|-----|--------|-------------|---------|
| **Usuario/Cliente** | `AFFILIATE` | Paciente o cliente que compra medicinas | ✅ Permitido |
| **Repartidor** | `DELIVERY` | Operador que realiza entregas | ✅ Permitido |
| **Administrador** | `ADMIN` | EPS - Empresa Promotora de Salud | ❌ No por registro |

**Nota:** Los usuarios ADMIN deben ser creados manualmente por otros admins en la base de datos.

---

## 📝 Cómo Registrarse Correctamente

### **Opción 1: Registrar un Cliente (AFFILIATE)**

```bash
URL: POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Andy",
  "email": "andy@gmail.com",
  "password": "mediG&2026",
  "role": "AFFILIATE"
}
```

**Respuesta esperada (201 Created):**
```json
{
  "id": 4,
  "name": "Andy",
  "email": "andy@gmail.com",
  "role": "AFFILIATE",
  "createdAt": "2026-04-06T10:30:00",
  "message": "Usuario registrado exitosamente"
}
```

---

### **Opción 2: Registrar un Repartidor (DELIVERY)**

```bash
URL: POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Carlos Repartidor",
  "email": "carlos@medigo.com",
  "password": "DeliveryPass#2026",
  "role": "DELIVERY"
}
```

**Respuesta esperada (201 Created):**
```json
{
  "id": 5,
  "name": "Carlos Repartidor",
  "email": "carlos@medigo.com",
  "role": "DELIVERY",
  "createdAt": "2026-04-06T10:35:00",
  "message": "Usuario registrado exitosamente"
}
```

---

## ⚠️ Errores Comunes y Soluciones

### **Error 1: Rol Inválido**
```json
400 Bad Request
{
  "message": "El rol 'ADMIN' no es válido. Solo se permiten: AFFILIATE (Usuario/Cliente), DELIVERY (Repartidor). ADMIN se crea por administradores."
}
```
**Solución:** Usa `"role": "AFFILIATE"` o `"role": "DELIVERY"`

---

### **Error 2: Contraseña Débil**
```json
400 Bad Request
{
  "message": "La contraseña debe tener al menos 8 caracteres, incluir mayúscula, minúscula y número"
}
```
**Requisitos de contraseña:**
- ✓ Mínimo 8 caracteres
- ✓ Al menos 1 mayúscula
- ✓ Al menos 1 minúscula
- ✓ Al menos 1 número

**Ejemplos válidos:**
- `mediG&2026` ✅
- `MyPass123` ✅
- `Secure@Pass456` ✅

---

### **Error 3: Email Duplicado**
```json
400 Bad Request
{
  "message": "El email ya está registrado"
}
```
**Solución:** Usa un email diferente que no exista en el sistema

---

### **Error 4: Email Inválido**
```json
400 Bad Request
{
  "message": "El email andy@gmail.com no es válido"
}
```
**Solución:** Usa formato válido: `nombre@dominio.com`

---

## 🔓 Cómo Crear un Usuario ADMIN

Si necesitas crear un admin, debes hacer INSERT directo en la BD:

```sql
-- Para Supabase
INSERT INTO users (username, email, password, role, active, created_at)
VALUES (
  'admin_nuevo',
  'admin_nuevo@medigo.com',
  'hashed_password_aqui',  -- BCrypt hash de tu contraseña
  'ADMIN',
  true,
  NOW()
);
```

O usa el endpoint de login con credenciales admin y luego crea admins desde la API interna (si está implementada).

---

## 🧪 Pruebas en Swagger

1. Abre: `http://localhost:8080/swagger-ui.html`
2. Busca: **POST /api/auth/register**
3. Haz clic en **"Try it out"**
4. Reemplaza el body con uno de los ejemplos anteriores
5. Haz clic en **"Execute"**

---

## 📚 Usuarios de Prueba Existentes

```
Email: admin@medigo.com
Pass:  123
Role:  ADMIN

Email: user@medigo.com
Pass:  123
Role:  AFFILIATE

Email: delivery@medigo.com
Pass:  123
Role:  DELIVERY
```

Usa estos para login (`POST /api/auth/login`) si quieres probar autenticación sin registrarte.
