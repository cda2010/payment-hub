(function () {
    'use strict';
    angular
            .module('ptiApp')
            .factory('ParametreApp', ParametreApp);

    ParametreApp.$inject = ['$resource', 'BASE_URL_API'];

    function ParametreApp($resource, BASE_URL_API) {
        var resourceUrl = BASE_URL_API + 'parametre-apps/:id';

        return $resource(resourceUrl, {}, {
            'query': {method: 'GET', isArray: true},
            'queryIsCheckCoreBanking': {method: 'GET', url: BASE_URL_API + 'parametre-apps/isCheckBanking/:codeFSE', isArray: false},
            'queryNomAndCode': {method: 'GET', url: BASE_URL_API + 'parametre-apps/nom/:nom/code/:codeFSE', isArray: false},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': {method: 'PUT'}
        });
    }
})();
