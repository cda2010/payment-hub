(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Notifications', Notifications);

    Notifications.$inject = ['$resource', 'DateUtils', 'BASE_URL_API'];

    function Notifications ($resource, DateUtils, BASE_URL_API) {
        var resourceUrl =  'api/notifications/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'sendSMS': {method: 'GET', url:BASE_URL_API +'omnnicanal/sendSms' ,isArray: false},
            'sendSMail': {method: 'GET', url:BASE_URL_API +'omnnicanal/mail' ,isArray: false},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dateHeureEnvoie = DateUtils.convertDateTimeFromServer(data.dateHeureEnvoie);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
