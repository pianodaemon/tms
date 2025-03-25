python3 -m venv venv

source venv/bin/activate

pip3 install -r requirements.txt

# It is set aside due to testability
pip3 install testcontainers==3.7.1

python3 -m unittest test_dozer.py
