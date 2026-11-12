(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PersonneController', PersonneController);

    PersonneController.$inject = ['$state', 'Personne', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function PersonneController($state, Personne, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;

    }
})();
