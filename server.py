#! /usr/bin/env python3.6
"""
Python 3.6 or newer required.
"""
from flask import Flask, render_template, jsonify, request
import json
import os
import stripe

# This is your test secret API key.
stripe.api_key = 'sk_test_51N5r1uSHE8F93FeUaAVT1055VyPDOS6LqynbfEIdmGxKZburyMiAIP16YQpucVuBEbHPVf6KTPQ6iIW0JwAM010D00ArJs5C4t'

app = Flask(__name__, static_folder='public',
            static_url_path='', template_folder='public')


def calculate_order_amount(items):
    # Replace this constant with a calculation of the order's amount
    # Calculate the order total on the server to prevent
    # people from directly manipulating the amount on the client
    amount = items[0]['amount']
    return amount*100


@app.route('/create-payment-intent', methods=['POST'])
def create_payment():
    try:
        data = json.loads(request.data)
        # Create a PaymentIntent with the order amount and currency
        intent = stripe.PaymentIntent.create(
            amount=calculate_order_amount(data['items']),
            currency='inr',
            automatic_payment_methods={
                'enabled': True,
            },
        )
        secret = jsonify({
            'clientSecret': intent['client_secret']
        })

        return secret
    except Exception as e:
        return jsonify(error=str(e)), 403


if __name__ == '__main__':
    app.run(port=4242)
