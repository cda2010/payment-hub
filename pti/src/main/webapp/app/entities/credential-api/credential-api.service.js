(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('CredentialApi', CredentialApi);

    CredentialApi.$inject = ['$resource'];

    function CredentialApi ($resource) {
        var resourceUrl =  'api/credential-apis/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'getAuthoritie': { method: 'GET',url: 'api/authority', isArray: true},
            'getOauthClientDet':{method:'GET',url: 'api/oauth-client-details', isArray: true},
            'getOneOauth':{method:'GET',url: 'api/oauth-client',isArray: false},
            'getClientSecret': {method:'GET', url:'api/oauth-client/secret', isArray: false},
            'getOauthById':
                {
                    method:'GET',
                    url: 'api/oauth-client',
                    isArray: false,
                    transformResponse: function (data) {
                        if (data) {
                            data = angular.fromJson(data);
                        }
                        return data;
                    }
                },
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
