-- =============================================
-- SEED DATA FOR ONLINE COURSE MANAGEMENT (MySQL)
-- All passwords are "password123" hashed with BCrypt (10 rounds)
-- =============================================

-- =============================================
-- USERS
-- =============================================
INSERT INTO users (email, password_hash, full_name, role) VALUES
-- Instructors
('alice.johnson@university.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyed8b8zXZQv0E9QxBp6L0qGH7aC0l5gGq', 'Alice Johnson', 'instructor'),
('bob.smith@university.edu',        '$2a$10$N9qo8uLOickgx2ZMRZoMyed8b8zXZQv0E9QxBp6L0qGH7aC0l5gGq', 'Bob Smith', 'instructor'),
('carol.davis@university.edu',       '$2a$10$N9qo8uLOickgx2ZMRZoMyed8b8zXZQv0E9QxBp6L0qGH7aC0l5gGq', 'Carol Davis', 'instructor'),
-- Students
('student1@email.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyed8b8zXZQv0E9QxBp6L0qGH7aC0l5gGq', 'John Doe',    'student'),
('student2@email.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyed8b8zXZQv0E9QxBp6L0qGH7aC0l5gGq', 'Jane Smith',  'student'),
('student3@email.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyed8b8zXZQv0E9QxBp6L0qGH7aC0l5gGq', 'Mike Brown',  'student'),
('student4@email.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyed8b8zXZQv0E9QxBp6L0qGH7aC0l5gGq', 'Emily Davis', 'student'),
('student5@email.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyed8b8zXZQv0E9QxBp6L0qGH7aC0l5gGq', 'Chris Lee',   'student');

-- =============================================
-- INSTRUCTORS
-- =============================================
INSERT INTO instructors (user_id, bio, expertise) VALUES
(1, 'Dr. Alice Johnson is a professor of Computer Science with over 15 years of experience in full-stack development, cloud computing, and software engineering. She has published numerous papers on distributed systems and leads research on scalable web architectures.', 'Full-Stack Development, Cloud Computing, System Design'),
(2, 'Prof. Bob Smith is a senior software engineer and educator specializing in modern JavaScript ecosystems, React architecture, and database design. He has mentored over 2,000 developers worldwide through online platforms.', 'JavaScript, React, Node.js, Database Design'),
(3, 'Dr. Carol Davis is a data scientist and instructor with expertise in Python, machine learning, and data engineering. She previously worked at major tech companies building large-scale data pipelines.', 'Python, Data Science, Machine Learning, SQL');

-- =============================================
-- STUDENTS
-- =============================================
INSERT INTO students (user_id, date_of_birth, phone, address) VALUES
(4, '2000-03-15', '+1-555-0101', '123 Maple Street, Boston, MA'),
(5, '1999-07-22', '+1-555-0102', '456 Oak Avenue, New York, NY'),
(6, '2001-11-08', '+1-555-0103', '789 Pine Road, Seattle, WA'),
(7, '1998-05-30', '+1-555-0104', '321 Cedar Lane, Austin, TX'),
(8, '2002-02-14', '+1-555-0105', '654 Birch Blvd, San Francisco, CA');

-- =============================================
-- COURSES
-- =============================================
INSERT INTO courses (instructor_id, title, description, price, thumbnail, is_published, category) VALUES
(1,
 'Complete Full-Stack Web Development',
 'Master modern web development from the ground up. This comprehensive course covers HTML, CSS, JavaScript, React, Node.js, Express, and PostgreSQL. Build real-world projects including e-commerce platforms, social media apps, and RESTful APIs.',
 149.99,
 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800',
 1,
 'Web Development'),

(2,
 'Advanced React & Node.js Masterclass',
 'Go beyond the basics with advanced patterns, hooks, state management, authentication, deployment strategies, and performance optimization. Includes TypeScript integration and testing best practices.',
 129.99,
 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800',
 1,
 'Web Development'),

(3,
 'Python for Data Science & Machine Learning',
 'A hands-on journey through Python, Pandas, NumPy, Matplotlib, Scikit-learn, and TensorFlow. Learn to analyze datasets, build predictive models, and create beautiful visualizations with real-world datasets.',
 179.99,
 'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800',
 1,
 'Data Science'),

(1,
 'DevOps & Cloud Engineering with AWS',
 'Learn Docker, Kubernetes, CI/CD pipelines, AWS services, infrastructure as code with Terraform, and monitoring. Deploy and scale applications confidently in the cloud.',
 199.99,
 'https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?w=800',
 1,
 'DevOps');

-- =============================================
-- ENROLLMENTS
-- =============================================
INSERT INTO enrollments (student_id, course_id, status) VALUES
(1, 1, 'active'),
(1, 2, 'active'),
(1, 3, 'completed'),
(2, 1, 'active'),
(2, 3, 'active'),
(3, 2, 'active'),
(3, 4, 'active'),
(4, 1, 'active'),
(4, 2, 'active'),
(4, 3, 'active'),
(5, 3, 'active'),
(5, 4, 'completed');

-- =============================================
-- MODULES (Course 1: Full-Stack Web Development)
-- =============================================
INSERT INTO modules (course_id, title, description, order_index) VALUES
(1, 'HTML5 & CSS3 Fundamentals', 'Learn the building blocks of every website — semantic HTML5 and modern CSS3 including Flexbox and Grid layouts.', 1),
(1, 'JavaScript Deep Dive', 'Master JavaScript ES6+, async/await, closures, prototypes, and the DOM API through hands-on exercises.', 2),
(1, 'React Essentials', 'Build dynamic user interfaces with React components, hooks, context, and state management patterns.', 3),
(1, 'Node.js & Express REST APIs', 'Create robust backend servers with Node.js, Express routing, middleware, and error handling.', 4),
(1, 'PostgreSQL & Database Design', 'Design normalized schemas, write complex SQL queries, and integrate with Node.js using pg.', 5),
(1, 'Full-Stack Final Project', 'Capstone project: build and deploy a complete full-stack application from scratch.', 6);

-- =============================================
-- MODULES (Course 2: Advanced React & Node.js)
-- =============================================
INSERT INTO modules (course_id, title, description, order_index) VALUES
(2, 'React Advanced Patterns', 'Explore compound components, render props, higher-order components, and performance patterns.', 1),
(2, 'State Management & TypeScript', 'Integrate TypeScript with React, Redux Toolkit, and React Query for scalable state management.', 2),
(2, 'Authentication & Security', 'Implement JWT auth, OAuth, protected routes, and secure API design patterns.', 3),
(2, 'Testing & Quality Assurance', 'Write unit, integration, and E2E tests with Jest, Testing Library, and Cypress.', 4),
(2, 'Performance & Optimization', 'Profile and optimize React apps with code splitting, lazy loading, and memoization.', 5);

-- =============================================
-- MODULES (Course 3: Python Data Science)
-- =============================================
INSERT INTO modules (course_id, title, description, order_index) VALUES
(3, 'Python Fundamentals for Data Science', 'Python core, data structures, list comprehensions, and scientific computing foundations.', 1),
(3, 'Data Manipulation with Pandas', 'Load, clean, transform, aggregate, and merge datasets efficiently with Pandas.', 2),
(3, 'Visualization with Matplotlib & Seaborn', 'Create publication-quality charts, plots, and dashboards for exploratory data analysis.', 3),
(3, 'Machine Learning Fundamentals', 'Supervised and unsupervised learning with Scikit-learn — regression, classification, clustering.', 4),
(3, 'Deep Learning with TensorFlow', 'Build and train neural networks, CNNs, and RNNs for image and sequence data.', 5);

-- =============================================
-- MODULES (Course 4: DevOps & Cloud)
-- =============================================
INSERT INTO modules (course_id, title, description, order_index) VALUES
(4, 'Docker & Containerization', 'Build, run, and compose Docker containers; multi-stage builds and container networking.', 1),
(4, 'Kubernetes Fundamentals', 'Deploy, scale, and manage containerized apps with Kubernetes pods, services, and ingress.', 2),
(4, 'CI/CD Pipelines', 'Automate testing and deployment with GitHub Actions, Jenkins, and ArgoCD workflows.', 3),
(4, 'AWS Core Services', 'EC2, S3, RDS, Lambda, CloudFront, Route 53, IAM — practical hands-on labs.', 4);

-- =============================================
-- ASSIGNMENTS
-- =============================================
INSERT INTO assignments (module_id, title, description, due_date, max_score) VALUES
-- Course 1
(1, 'Build a Responsive Portfolio Page', 'Create a personal portfolio page using HTML5 and CSS3 Grid/Flexbox. Must be mobile-responsive with at least 3 sections.', '2026-06-01 23:59:00', 100),
(2, 'JavaScript Todo App', 'Build a fully functional todo application with localStorage persistence, filtering, and dark mode toggle using vanilla JavaScript.', '2026-06-08 23:59:00', 100),
(3, 'React Weather Dashboard', 'Create a weather dashboard using the OpenWeatherMap API. Include search, 5-day forecast, and loading/error states.', '2026-06-15 23:59:00', 100),
(4, 'RESTful API with Express', 'Build a blog API supporting CRUD operations with Express, with proper validation and error handling.', '2026-06-22 23:59:00', 100),
(5, 'E-commerce Database Schema', 'Design and implement a PostgreSQL database schema for an e-commerce platform. Write join queries and indexes.', '2026-06-29 23:59:00', 100),
-- Course 2
(7, 'Advanced Component Library', 'Build a reusable UI component library with TypeScript, covering buttons, modals, forms, and data tables.', '2026-06-10 23:59:00', 100),
(8, 'JWT Authentication System', 'Implement a complete auth system with registration, login, JWT tokens, refresh tokens, and protected routes.', '2026-06-17 23:59:00', 100),
(9, 'End-to-End Test Suite', 'Write a comprehensive Cypress test suite for a React app covering login, data submission, and error states.', '2026-06-24 23:59:00', 100),
-- Course 3
(11, 'Pandas Data Exploration Project', 'Analyze a real-world dataset (e.g., Titanic or housing prices). Produce summary stats, clean data, and key insights.', '2026-06-05 23:59:00', 100),
(12, 'Interactive Data Visualization', 'Create a dynamic dashboard with at least 4 different chart types using Matplotlib/Seaborn.', '2026-06-12 23:59:00', 100),
-- Course 4
(15, 'Dockerize a Full-Stack App', 'Containerize a Node.js + React application using Docker with a multi-stage build and docker-compose.', '2026-06-08 23:59:00', 100),
(16, 'Deploy to Kubernetes Cluster', 'Deploy the Dockerized app to a Kubernetes cluster with a Service, Deployment, and ConfigMap.', '2026-06-15 23:59:00', 100);

-- =============================================
-- SUBMISSIONS
-- =============================================
INSERT INTO submissions (assignment_id, student_id, submission_text, score, feedback, submitted_at, graded_at) VALUES
(1, 1, 'https://github.com/student1/portfolio', 95, 'Excellent work! Clean design, great responsiveness, and well-structured HTML. Minor: add more hover effects.', '2026-05-25 14:30:00', '2026-05-26 09:15:00'),
(1, 2, 'https://github.com/student2/portfolio', 87, 'Good job overall. Consider improving color contrast and adding smoother transitions.', '2026-05-26 18:45:00', '2026-05-27 11:00:00'),
(1, 4, 'https://github.com/student4/portfolio', 78, 'Basic but functional. Work on the mobile layout and add more visual polish.', '2026-05-27 10:00:00', '2026-05-28 14:30:00'),
(2, 1, 'https://github.com/student1/todo-app', 92, 'Great implementation! The dark mode and filtering features work perfectly. Well done.', '2026-06-01 22:00:00', '2026-06-02 10:30:00'),
(2, 2, 'https://github.com/student2/todo-app', 88, 'Solid todo app. The localStorage persistence is working well. Could add animations for better UX.', '2026-06-02 20:00:00', '2026-06-03 09:00:00'),
(3, 1, 'https://github.com/student1/weather-dash', 90, 'Beautiful UI and good error handling. Try adding caching for API calls to improve performance.', '2026-06-10 15:00:00', '2026-06-11 11:45:00'),
(9, 1, 'https://github.com/student1/pandas-project', 98, 'Outstanding analysis! Great insights, clean code, and comprehensive visualizations.', '2026-06-04 12:00:00', '2026-06-05 08:30:00'),
(9, 2, 'https://github.com/student2/pandas-project', 85, 'Good analysis. Add more explanatory comments and interpret your findings more thoroughly.', '2026-06-05 19:30:00', '2026-06-06 10:00:00');

-- =============================================
-- PAYMENTS
-- =============================================
INSERT INTO payments (student_id, course_id, amount, currency, status, payment_method, transaction_id, paid_at) VALUES
(1, 1, 149.99, 'USD', 'completed', 'card', 'txn_001_abc123', '2026-04-10 10:30:00'),
(1, 2, 129.99, 'USD', 'completed', 'card', 'txn_002_def456', '2026-04-12 14:15:00'),
(2, 1, 149.99, 'USD', 'completed', 'card', 'txn_003_ghi789', '2026-04-15 09:00:00'),
(2, 3, 179.99, 'USD', 'completed', 'paypal', 'txn_004_jkl012', '2026-04-16 16:45:00'),
(3, 2, 129.99, 'USD', 'completed', 'card', 'txn_005_mno345', '2026-04-18 11:20:00'),
(4, 1, 149.99, 'USD', 'completed', 'card', 'txn_006_pqr678', '2026-04-20 08:30:00'),
(4, 2, 129.99, 'USD', 'completed', 'card', 'txn_007_stu901', '2026-04-22 17:00:00'),
(5, 3, 179.99, 'USD', 'completed', 'card', 'txn_008_vwx234', '2026-04-25 13:45:00'),
(5, 4, 199.99, 'USD', 'completed', 'paypal', 'txn_009_yza567', '2026-04-27 10:00:00');