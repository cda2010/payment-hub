(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ReleveCompteDetailController', ReleveCompteDetailController);

    ReleveCompteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ReleveCompte', 'DetailReleveCompte'];

    function ReleveCompteDetailController($scope, $rootScope, $stateParams, previousState, entity, ReleveCompte, DetailReleveCompte) {
        var vm = this;

        vm.releveCompte = entity;
        vm.previousState = previousState.name;
        vm.transactions=DetailReleveCompte.queryByReleve({releveId:entity.id});
        var unsubscribe = $rootScope.$on('ptiApp:releveCompteUpdate', function(event, result) {
            vm.releveCompte = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
