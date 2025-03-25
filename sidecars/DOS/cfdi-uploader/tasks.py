from celery import Celery
import boto3
import requests
import os

redis_host = os.getenv("REDIS_HOST", "localhost")
redis_port = os.getenv("REDIS_PORT", "6379")

# Initialize Celery with Redis as the broker
app = Celery(
    "tasks",
    broker=f"redis://{redis_host}:{redis_port}/1",
    backend=f"redis://{redis_host}:{redis_port}/2")

@app.task(name='uploadToBucketTask')
def upload_to_bucket(path_prefix, uuid, result_type, host, third_party_keys):

    headers = {
        'Content-Type': 'application/json',
    }
    headers.update(third_party_keys)

    url = f"https://{host}/v4/cfdi40/{uuid}/{result_type}"
    response = requests.get(url, headers=headers)

    bucket_url = ""
    if response.status_code == 200:
        bucket_url = _bucketize(path_prefix, uuid, result_type, response.content)

    return bucket_url


def _bucketize(path_prefix, uuid, result_type, file_data):
    """
    Decode a Base64 string, save it as a file, and upload it to an S3 bucket.

    :param path_prefix: The prefix path within the bucket where the file will be stored.
    :param uuid: A unique identifier for naming the file.
    :param base64str: The Base64-encoded string representing the file content.
    """

    # Initialize the S3 client
    s3 = boto3.client("s3")

    # Define the bucket name and the S3 path
    bucket_name = os.getenv("BUCKET_NAME")  # Assumes bucket name is stored in an environment variable
    if not bucket_name:
        raise ValueError("BUCKET_NAME environment variable is not set")

    file_name = f"{uuid}.{result_type}"
    s3_path = os.path.join(path_prefix, file_name)

    # Upload the data to S3 directly
    try:
        s3.put_object(Bucket=bucket_name, Key=s3_path, Body=file_data)
    except boto3.exceptions.S3UploadFailedError as e:
        raise RuntimeError(f"Failed to upload data to S3: {e}") from e

    return f"s3://{bucket_name}/{s3_path}"


@app.task(name='spamTheWorld', ignore_result=True)
def spam_the_world(owner: str, receptor_rfc: str, doc_uuid: str, receipt_id: str):
    """
    Sends an HTTP POST request to the Lego Mail Composer service to trigger an email delivery.

    :param owner: The identifier for the owner, used in the API URL.
    :param receptor_rfc: The RFC (Registro Federal de Contribuyentes) of the recipient.
    :param doc_uuid: The unique identifier of the document being sent.
    :param receipt_id: The unique identifier for the receipt.

    :raises ValueError: If either `HTTP_LEGO_MAIL_COMPOSER_HOST` or `HTTP_LEGO_MAIL_COMPOSER_PORT`
                         environment variables are not set.
    :raises RuntimeError: If the HTTP request fails due to network or API errors.

    This function runs asynchronously as a Celery task and does not return any result. Any failure in
    making the HTTP request will raise an exception.
    """
    msg_content = {
        "receptor_rfc": receptor_rfc,
        "doc_uuid": doc_uuid,
        "receipt_id": receipt_id
    }

    service_host = os.getenv("HTTP_LEGO_MAIL_COMPOSER_HOST")
    service_port = os.getenv("HTTP_LEGO_MAIL_COMPOSER_PORT")
    if not service_host:
        raise ValueError("HTTP_LEGO_MAIL_COMPOSER_HOST environment variable is not set")
    if not service_port:
        raise ValueError("HTTP_LEGO_MAIL_COMPOSER_PORT environment variable is not set")

    url = f"http://{service_host}:{service_port}/v1/deliveries/{owner}"
    headers = {"Content-Type": "application/json"}

    try:
        res = requests.post(url, json=msg_content, headers=headers)
        res.raise_for_status()
    except requests.RequestException as e:
        raise RuntimeError(f"Failed to make the request: {e}") from e
