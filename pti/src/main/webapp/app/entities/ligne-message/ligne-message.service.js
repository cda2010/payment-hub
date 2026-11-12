(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('LigneMessage', LigneMessage);

    LigneMessage.$inject = ['$resource','BASE_URL_API'];

    function LigneMessage ($resource,BASE_URL_API) {
        var resourceUrl =  'api/ligne-messages/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'getLigneMessage': {method: 'GET', url: BASE_URL_API+'findByChargementId' ,isArray: true},
            'deleteChargement' : {method: 'GET', url: BASE_URL_API + 'deleteChargement', isArray: false},
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
