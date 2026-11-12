(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailReleveCompteDetailController', DetailReleveCompteDetailController);

    DetailReleveCompteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'DetailReleveCompte', 'ReleveCompte'];

    function DetailReleveCompteDetailController($scope, $rootScope, $stateParams, previousState, entity, DetailReleveCompte, ReleveCompte) {
        var vm = this;

        vm.detailReleveCompte = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:detailReleveCompteUpdate', function(event, result) {
            vm.detailReleveCompte = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
