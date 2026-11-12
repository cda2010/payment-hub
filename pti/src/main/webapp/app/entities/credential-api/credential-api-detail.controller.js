(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CredentialApiDetailController', CredentialApiDetailController);

    CredentialApiDetailController.$inject = [ '$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'CredentialApi', 'TypeUOWithoutUR', 'TypeUniteOrganisation', 'UniteOrganisationByType', 'UniteOrganisation', 'RequestUtils'];

    function CredentialApiDetailController( $scope, $rootScope, $stateParams,  previousState, entity, CredentialApi, TypeUOWithoutUR, TypeUniteOrganisation, UniteOrganisationByType, UniteOrganisation, RequestUtils) {
        var vm = this;

        vm.credentialApi = entity;
        vm.previousState = previousState.name;
        vm.clear = clear;
        vm.profils = [];
        vm.organisationFSE = [];
        vm.typeUniteOrganisations = [];
        vm.codeTypeUO= null;
        console.log('---detail---',vm.credentialApi);
        if (vm.credentialApi.scope!=null) {vm.credentialApi.scope=vm.credentialApi.scope.split(',');}
        if (vm.credentialApi.authorities!=null) {vm.credentialApi.authorities=vm.credentialApi.authorities.split(',');}
        if(vm.credentialApi.authorized_grant_types!=null) {vm.credentialApi.authorized_grant_types=vm.credentialApi.authorized_grant_types.split(',');}
        console.log('---scope---',vm.credentialApi.scope);
        var unsubscribe = $rootScope.$on('ptiApp:credentialApiUpdate', function(event, result) {
            vm.credentialApi = result;
            console.log('---detail1---',vm.credentialApi);
        });
        $scope.$on('$destroy', unsubscribe);
        function clear() {
            console.log('shut');
           // $uibModalInstance.dismiss('cancel');
        }
        if (angular.isDefined(vm.credentialApi.uniteOrganisation)){
            console.log('---Get UO -', vm.credentialApi.uniteOrganisation.id);
            vm.codeTypeUO = vm.credentialApi.uniteOrganisation.typeUO.id;
            vm.organisationFSE.push(vm.credentialApi.uniteOrganisation);
            vm.typeUniteOrganisations.push(vm.credentialApi.uniteOrganisation.typeUO);
        }

        if (angular.isDefined(vm.credentialApi.profil)){
            vm.profils.push(vm.credentialApi.profil);
            vm.credentialApi.profilId = vm.credentialApi.profil.id;
        }
    }
})();
