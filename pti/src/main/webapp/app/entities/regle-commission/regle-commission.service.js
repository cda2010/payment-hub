(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('RegleCommission', RegleCommission);

    RegleCommission.$inject = ['$resource'];

    function RegleCommission ($resource) {
        var resourceUrl =  'api/regle-commissions/:id';

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
