(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ReleveCompte', ReleveCompte);

    ReleveCompte.$inject = ['$resource', 'DateUtils'];

    function ReleveCompte ($resource, DateUtils) {
        var resourceUrl =  'api/releve-comptes/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dateDebut = DateUtils.convertDateTimeFromServer(data.dateDebut);
                        data.dateFin = DateUtils.convertDateTimeFromServer(data.dateFin);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
