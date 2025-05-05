-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    create_date TIMESTAMP NOT NULL,
    update_date TIMESTAMP NOT NULL
);

-- Projects table
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id BIGINT NOT NULL,
    create_date TIMESTAMP NOT NULL,
    update_date TIMESTAMP NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- Tasks table
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    due_date DATE,
    priority VARCHAR(20) NOT NULL,
    project_id BIGINT NOT NULL,
    assigned_user_id BIGINT,
    create_date TIMESTAMP NOT NULL,
    update_date TIMESTAMP NOT NULL,
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (assigned_user_id) REFERENCES users(id)
);

-- Indexes
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_project_owner ON projects(owner_id);
CREATE INDEX idx_task_project ON tasks(project_id);
CREATE INDEX idx_task_assigned_user ON tasks(assigned_user_id);
CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_priority ON tasks(priority); 