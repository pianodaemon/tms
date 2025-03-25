# Use an official Python runtime as a parent image
FROM python:3.13-slim-bookworm

# Set the working directory
WORKDIR /usr/src/app

# Copy the requirements file into the container
COPY requirements.txt ./

# Install any dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the current directory contents into the container
COPY . .

RUN chmod +x run.sh

# Define environment variable for Celery
ENV PYTHONUNBUFFERED=1
