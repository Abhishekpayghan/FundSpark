# 🚀 FundSpark - Crowdfunding & Startup Investment Platform

A complete, production-ready **crowdfunding platform** connecting startup founders with investors. Features founder dashboards, investor portfolio management, admin analytics, and real-time notifications.

## 📋 Project Overview

FundSpark is a full-stack application built with:
- **Frontend**: HTML5, CSS3, JavaScript (no frameworks)
- **Backend**: Java Spring Boot, MySQL, JPA/Hibernate
- **Architecture**: RESTful API with component-based UI

### Key Features

#### 👥 For Founders
- Create and manage startup campaigns
- Track investor interest and funding progress
- View analytics and investor profiles
- Receive notifications on new investments

#### 💼 For Investors
- Browse and filter startup opportunities
- Manage investment portfolio
- Follow startups and save wishlist
- Connect with founders

#### 🛠️ For Admins
- Dashboard analytics and reporting
- User and startup management
- Campaign approval workflow
- Category management

---

## 🗂️ Project Structure

```
fundspark/
├── fundspark-backend/          # Java Spring Boot API
│   ├── src/main/java/
│   │   └── com/fundspark/fundspark_backend/
│   │       ├── controller/     # REST endpoints
│   │       ├── model/          # Entity classes
│   │       ├── repository/     # Data access layer
│   │       └── service/        # Business logic
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── database/
│   │   ├── schema.sql          # Database schema
│   │   └── seed.sql            # Initial data
│   └── pom.xml                 # Maven configuration
│
├── fundspark-frontend/         # Frontend static files
│   └── fundspark/
│       ├── index.html          # Landing page
│       ├── explore.html        # Browse startups
│       ├── startup-details.html # Campaign details
│       ├── founder/            # Founder dashboard
│       ├── investor/           # Investor dashboard
│       ├── admin/              # Admin dashboard
│       └── assets/
│           ├── css/
│           │   ├── style.css   # Global styles
│           │   └── dashboard.css # Dashboard layout
│           └── js/
│               ├── data.js     # Mock data
│               ├── api.js      # API client
│               ├── auth.js     # Authentication
│               ├── components.js
│               └── main.js     # UI interactions
│
├── postman/                    # API documentation & tests
├── SETUP.md                    # Environment configuration guide
└── README.md                   # This file
```

---

## ⚙️ System Requirements

### Backend
- **Java**: 17+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Spring Boot**: 4.0.7

### Frontend
- Modern browser (Chrome, Firefox, Safari, Edge)
- No build tools or package managers required

---

## 🚀 Getting Started

### 1. Clone Repository
```bash
git clone https://github.com/Abhishekpayghan/FundSpark.git
cd fundspark
```

### 2. Setup Database

#### Create MySQL Database
```bash
mysql -u root -p
```

```sql
CREATE DATABASE fundspark;
USE fundspark;
```

#### Load Schema
```bash
mysql -u root -p fundspark < fundspark-backend/database/schema.sql
mysql -u root -p fundspark < fundspark-backend/database/seed.sql
```

### 3. Configure Backend

See [SETUP.md](./SETUP.md) for detailed environment configuration.

**Quick Setup:**
```bash
# Create local properties file with your credentials
cat > fundspark-backend/src/main/resources/application-local.properties << EOF
spring.datasource.username=root
spring.datasource.password=your_password_here
EOF
```

### 4. Run Backend

```bash
cd fundspark-backend
mvn clean install
mvn spring-boot:run
```

Backend will start at `http://localhost:8080`

### 5. Run Frontend

**Option A: Simple HTTP Server**
```bash
cd fundspark-frontend/fundspark
python -m http.server 8000
```

**Option B: Node.js Serve**
```bash
cd fundspark-frontend/fundspark
npx serve .
```

**Option C: Direct File**
```bash
cd fundspark-frontend/fundspark
open index.html  # macOS
# or double-click on Windows/Linux
```

Frontend will be available at `http://localhost:8000` (or `http://localhost:3000` with npx serve)

---

## 🔌 API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/logout` - User logout

### Startups
- `GET /api/startups` - List all startups
- `GET /api/startups/{id}` - Get startup details
- `POST /api/startups` - Create startup (founder)
- `PUT /api/startups/{id}` - Update startup
- `DELETE /api/startups/{id}` - Delete startup

### Users
- `GET /api/users/{id}` - Get user profile
- `PUT /api/users/{id}` - Update user profile

### Investments
- `GET /api/investments` - List investments
- `POST /api/investments` - Create investment
- `GET /api/investments/{id}` - Investment details

### Notifications
- `GET /api/notifications` - List notifications
- `PUT /api/notifications/{id}` - Mark as read

### Categories
- `GET /api/categories` - List categories

### Comments
- `GET /api/comments` - List comments
- `POST /api/comments` - Create comment

See [Postman Collection](./postman/) for complete API documentation.

---

## 🔐 Security

- Database credentials managed via environment variables
- Password hashing for user authentication
- API authentication tokens
- CORS configured for allowed origins
- SQL injection prevention via parameterized queries
- XSS protection via output encoding

**⚠️ Important:** Never commit `.env` files or credentials. See [SETUP.md](./SETUP.md) for secure configuration.

---

## 📊 Database Schema

### Core Tables
- **users** - Platform users (founder/investor/admin)
- **startups** - Founder campaigns
- **categories** - Startup categories
- **investments** - Investment records
- **comments** - Startup comments
- **notifications** - User notifications
- **wishlist** - Saved startups

See [schema.sql](./fundspark-backend/database/schema.sql) for complete schema.

---

## 🧪 Testing

### Backend Unit Tests
```bash
cd fundspark-backend
mvn test
```

### API Testing with Postman
Import collection from `postman/collections/` into Postman and test endpoints.

---

## 📝 Code Style & Conventions

### Backend (Java)
- Spring Boot best practices
- RESTful API conventions
- Repository pattern for data access
- Service layer for business logic

### Frontend (JavaScript)
- Vanilla JavaScript (no frameworks)
- Component-based architecture
- Mock data in `data.js`
- API client abstraction in `api.js`

---

## 🤝 Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Make your changes and commit: `git commit -m "Add your feature"`
3. Push to GitHub: `git push origin feature/your-feature`
4. Open a Pull Request

### Commit Message Format
```
type: subject line (50 chars max)

Detailed description of changes (if needed)
- Bullet point 1
- Bullet point 2
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

---

## 📚 Documentation

- [SETUP.md](./SETUP.md) - Environment setup and configuration
- [schema.sql](./fundspark-backend/database/schema.sql) - Database schema
- [API Postman Collection](./postman/) - Complete API documentation

---

## 🐛 Troubleshooting

### Backend won't start
- Check MySQL is running: `mysql -u root -p`
- Verify database exists: `SHOW DATABASES;`
- Check application.properties or environment variables
- Review `SETUP.md` for configuration

### Frontend can't connect to API
- Ensure backend is running on port 8080
- Check browser console for CORS errors
- Verify API endpoint URLs in `assets/js/api.js`

### Database connection errors
- Verify username/password in `application-local.properties`
- Check database name is `fundspark`
- Ensure MySQL service is running

---

## 📅 Roadmap

- [ ] Payment gateway integration (Stripe/Razorpay)
- [ ] Email notifications
- [ ] Social media sharing
- [ ] Advanced analytics
- [ ] Mobile app
- [ ] Video KYC verification

---

## 📄 License

MIT License - see LICENSE file for details

---

## 👨‍💻 Author

**Abhishek Payghan**
- GitHub: [@Abhishekpayghan](https://github.com/Abhishekpayghan)
- Email: abhishekpayghan@example.com

---

## 🙏 Acknowledgments

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [Lucide Icons](https://lucide.dev/)
- [Chart.js](https://www.chartjs.org/)

---

## 📞 Support & Feedback

Found a bug or have a feature request? [Open an issue](https://github.com/Abhishekpayghan/FundSpark/issues)

---

**Happy coding! 🎉**
