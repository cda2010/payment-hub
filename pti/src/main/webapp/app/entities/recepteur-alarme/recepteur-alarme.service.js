(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('RecepteurAlarme', RecepteurAlarme);

    RecepteurAlarme.$inject = ['$resource'];

    function RecepteurAlarme ($resource) {
        var resourceUrl =  'api/recepteur-alarmes/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
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
