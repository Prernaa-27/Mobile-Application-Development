-- =========================================
-- UNIGLOBE DATABASE (Android SQLite Version)
-- =========================================

-- =========================
-- TABLES
-- =========================

CREATE TABLE IF NOT EXISTS Universities (
    university_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    location TEXT NOT NULL,
    fees INTEGER NOT NULL,
    university_type TEXT CHECK(university_type IN ('Public','Private')),
    overall_score REAL,
    employment_outcomes REAL,
    website_url TEXT,
    information TEXT
);

CREATE TABLE IF NOT EXISTS Programs (
    program_id INTEGER PRIMARY KEY AUTOINCREMENT,
    university_id INTEGER,
    course TEXT NOT NULL,
    degree_level TEXT CHECK(degree_level IN ('UG','PG')),
    duration_years INTEGER,
    FOREIGN KEY (university_id) REFERENCES Universities(university_id)
);

CREATE TABLE IF NOT EXISTS Counsellors (
    counsellor_id INTEGER PRIMARY KEY AUTOINCREMENT,
    university_id INTEGER,
    name TEXT NOT NULL,
    email TEXT NOT NULL,
    phone_number TEXT,
    FOREIGN KEY (university_id) REFERENCES Universities(university_id)
);

CREATE TABLE IF NOT EXISTS Saved_Universities (
    save_id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_email TEXT NOT NULL,
    university_id INTEGER,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (university_id) REFERENCES Universities(university_id)
);

-- =========================
-- SAMPLE DATA
-- =========================

INSERT INTO Universities (name, location, fees, university_type, overall_score, employment_outcomes, website_url, information)
VALUES
('MIT', 'USA', 5000000, 'Private', 9.5, 9.2, 'https://web.mit.edu', 'Top engineering university with cutting-edge research and innovation'),
('Oxford', 'UK', 4000000, 'Public', 9.3, 9.0, 'https://www.ox.ac.uk', 'World-renowned institution with centuries of academic excellence'),
('Stanford', 'USA', 4800000, 'Private', 9.4, 9.1, 'https://www.stanford.edu', 'Leading university in innovation and entrepreneurship'),
('Toronto', 'Canada', 3000000, 'Public', 8.8, 8.5, 'https://www.utoronto.ca', 'Top Canadian research university with excellent global reputation'),
('NUS', 'Singapore', 3500000, 'Public', 9.0, 8.7, 'https://www.nus.edu.sg', 'Leading global university in Asia with strong research programs'),
('Harvard University', 'USA', 5200000, 'Private', 9.6, 9.3, 'https://www.harvard.edu', 'Prestigious Ivy League institution with world-class faculty'),
('Cambridge University', 'UK', 4100000, 'Public', 9.4, 9.1, 'https://www.cam.ac.uk', 'Historic university with outstanding academic excellence'),
('ETH Zurich', 'Switzerland', 2000000, 'Public', 9.2, 9.0, 'https://ethz.ch', 'Top European tech institute specializing in engineering and sciences'),
('University of Melbourne', 'Australia', 2800000, 'Public', 8.9, 8.6, 'https://www.unimelb.edu.au', 'Leading Australian university with strong international presence'),
('UCLA', 'USA', 4500000, 'Public', 9.1, 8.9, 'https://www.ucla.edu', 'Top US public university with diverse programs'),
('University of Tokyo', 'Japan', 3000000, 'Public', 9.0, 8.8, 'https://www.u-tokyo.ac.jp', 'Leading Asian university with excellence in research'),
('Imperial College London', 'UK', 4200000, 'Public', 9.3, 9.1, 'https://www.imperial.ac.uk', 'Top STEM university in the UK'),
('University of Chicago', 'USA', 4900000, 'Private', 9.2, 9.0, 'https://www.uchicago.edu', 'Top research institution with strong academic programs'),
('Tsinghua University', 'China', 2500000, 'Public', 9.1, 8.9, 'https://www.tsinghua.edu.cn', 'Top Chinese university known for engineering and technology'),
('University of British Columbia', 'Canada', 2700000, 'Public', 8.8, 8.6, 'https://www.ubc.ca', 'Leading Canadian university with beautiful campus'),
('Seoul National University', 'South Korea', 2300000, 'Public', 8.9, 8.7, 'https://www.snu.ac.kr', 'Top Korean university with strong research focus'),
('Paris Sciences et Lettres', 'France', 2600000, 'Public', 8.8, 8.6, 'https://psl.eu', 'Top French university combining arts and sciences'),
('University of Amsterdam', 'Netherlands', 2400000, 'Public', 8.7, 8.5, 'https://www.uva.nl', 'European research university with diverse programs'),
('Monash University', 'Australia', 2600000, 'Public', 8.7, 8.5, 'https://www.monash.edu', 'Top Australian university with global campuses'),
('University of Edinburgh', 'UK', 3900000, 'Public', 9.0, 8.8, 'https://www.ed.ac.uk', 'Historic UK university with strong research tradition');

INSERT INTO Programs (university_id, course, degree_level, duration_years) VALUES
(1, 'Computer Science', 'UG', 4),
(1, 'Computer Science', 'PG', 2),
(1, 'Engineering', 'UG', 4),
(1, 'Data Science', 'PG', 2),
(1, 'Physics', 'UG', 4),
(2, 'Law', 'UG', 3),
(2, 'Law', 'PG', 2),
(2, 'Medicine', 'UG', 5),
(2, 'Philosophy', 'PG', 2),
(2, 'Engineering', 'UG', 4),
(3, 'Engineering', 'UG', 4),
(3, 'Business', 'PG', 2),
(3, 'Computer Science', 'UG', 4),
(3, 'Medicine', 'UG', 5),
(4, 'Business', 'PG', 2),
(4, 'Medicine', 'UG', 5),
(4, 'Engineering', 'UG', 4),
(4, 'Arts', 'UG', 3),
(5, 'Computer Science', 'UG', 4),
(5, 'Engineering', 'UG', 4),
(5, 'Business', 'PG', 2),
(5, 'Data Science', 'PG', 2),
(6, 'Law', 'PG', 2),
(6, 'Medicine', 'UG', 5),
(6, 'Business', 'PG', 2),
(6, 'Computer Science', 'UG', 4),
(7, 'Engineering', 'UG', 4),
(7, 'Mathematics', 'UG', 3),
(7, 'Medicine', 'UG', 5),
(7, 'Computer Science', 'UG', 4),
(8, 'Engineering', 'UG', 4),
(8, 'Physics', 'UG', 4),
(8, 'Computer Science', 'UG', 4),
(9, 'Medicine', 'UG', 5),
(9, 'Law', 'UG', 3),
(9, 'Engineering', 'UG', 4),
(9, 'Business', 'PG', 2),
(10, 'Engineering', 'UG', 4),
(10, 'Medicine', 'UG', 5),
(10, 'Arts', 'UG', 3),
(11, 'Engineering', 'UG', 4),
(11, 'Medicine', 'UG', 5),
(11, 'Computer Science', 'UG', 4),
(12, 'Engineering', 'UG', 4),
(12, 'Medicine', 'UG', 5),
(12, 'Computer Science', 'UG', 4),
(13, 'Business', 'PG', 2),
(13, 'Law', 'PG', 2),
(13, 'Arts', 'UG', 3),
(14, 'Engineering', 'UG', 4),
(14, 'Computer Science', 'UG', 4),
(15, 'Engineering', 'UG', 4),
(15, 'Medicine', 'UG', 5),
(15, 'Arts', 'UG', 3),
(16, 'Engineering', 'UG', 4),
(16, 'Medicine', 'UG', 5),
(17, 'Arts', 'UG', 3),
(17, 'Law', 'PG', 2),
(18, 'Engineering', 'UG', 4),
(18, 'Law', 'PG', 2),
(19, 'Engineering', 'UG', 4),
(19, 'Medicine', 'UG', 5),
(19, 'Business', 'PG', 2),
(20, 'Medicine', 'UG', 5),
(20, 'Engineering', 'UG', 4),
(20, 'Arts', 'UG', 3);

INSERT INTO Counsellors (university_id, name, email, phone_number) VALUES
(1, 'John Smith', 'john.smith@mit.edu', '+1-617-253-1000'),
(2, 'Emma Watson', 'emma.watson@ox.ac.uk', '+44-1865-270000'),
(3, 'Michael Chen', 'michael.chen@stanford.edu', '+1-650-723-2300'),
(4, 'Sarah Johnson', 'sarah.johnson@utoronto.ca', '+1-416-978-2011'),
(5, 'David Lee', 'david.lee@nus.edu.sg', '+65-6516-6666'),
(6, 'Jennifer Brown', 'jennifer.brown@harvard.edu', '+1-617-495-1000'),
(7, 'Robert Wilson', 'robert.wilson@cam.ac.uk', '+44-1223-337733'),
(8, 'Maria Garcia', 'maria.garcia@ethz.ch', '+41-44-632-1111'),
(9, 'James Taylor', 'james.taylor@unimelb.edu.au', '+61-3-9035-5511'),
(10, 'Lisa Anderson', 'lisa.anderson@ucla.edu', '+1-310-825-4321'),
(11, 'Yuki Tanaka', 'yuki.tanaka@u-tokyo.ac.jp', '+81-3-3812-2111'),
(12, 'Thomas Moore', 'thomas.moore@imperial.ac.uk', '+44-20-7589-5111'),
(13, 'Emily Davis', 'emily.davis@uchicago.edu', '+1-773-702-1234'),
(14, 'Wei Zhang', 'wei.zhang@tsinghua.edu.cn', '+86-10-6279-5001'),
(15, 'Amanda White', 'amanda.white@ubc.ca', '+1-604-822-2211'),
(16, 'Kim Min-ho', 'kim.minho@snu.ac.kr', '+82-2-880-5114'),
(17, 'Sophie Martin', 'sophie.martin@psl.eu', '+33-1-71-93-11-00'),
(18, 'Peter van Dijk', 'peter.vandijk@uva.nl', '+31-20-525-9111'),
(19, 'Rachel Green', 'rachel.green@monash.edu', '+61-3-9905-4000'),
(20, 'Andrew Scott', 'andrew.scott@ed.ac.uk', '+44-131-650-1000');
