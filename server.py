#! /usr/bin/env python3.6
"""
Python 3.6 or newer required.
"""
from flask import Flask, render_template, jsonify, request
import json
import os
import stripe

# This is your test secret API key.
stripe.api_key = '<SECRET_API_KEY>'

app = Flask(__name__, static_folder='public',
            static_url_path='', template_folder='public')


def calculate_order_amount(items):
    # Replace this constant with a calculation of the order's amount
    # Calculate the order total on the server to prevent
    # people from directly manipulating the amount on the client
    amount = items[0]['amount']
    return amount*100


@app.route('/create_payment_intent', methods=['POST'])
def create_payment():
    try:
        print(request.data)
        data = json.loads(request.data)
        # Create a PaymentIntent with the order amount and currency
        intent = stripe.PaymentIntent.create(
            amount=calculate_order_amount(data['items']),
            currency="usd",
            automatic_payment_methods={
                "enabled": True,
            },
        )
        print(intent)
        secret = jsonify({
            'client_secret': intent['client_secret']
        })

        return secret
    except Exception as e:
        return jsonify(error=str(e)), 403


if __name__ == '__main__':
    app.run(port=4242)
