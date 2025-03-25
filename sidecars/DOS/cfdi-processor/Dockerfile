FROM python:3.13-slim-bookworm

ENV APP_DIR=/
COPY requirements.txt $APP_DIR/requirements.txt
COPY processor.py $APP_DIR/processor.py
COPY run.sh $APP_DIR/run.sh
RUN chmod +x $APP_DIR/run.sh
WORKDIR $APP_DIR

RUN python -m pip install --upgrade pip
RUN python -m pip install -r requirements.txt

CMD [ "/run.sh" ]
