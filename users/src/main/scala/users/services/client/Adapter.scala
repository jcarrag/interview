package users.services.client

import users.domain._

object Adapter {

  // ClientDomain to UserDomain adapters
  trait DomainAdapter[C <: ClientDomain, D <: Domain] {
    def toDomain(client: C): D
  }

  implicit class DomainAdapterGen[C <: ClientDomain, D <: Domain](client: C)(implicit adapter: DomainAdapter[C, D]) {
    def toDomain():D = adapter.toDomain(client)
  }

  implicit object EmailDomainAdapter extends DomainAdapter[ClientDomain.EmailAddress, EmailAddress] {
    def toDomain(client: ClientDomain.EmailAddress): EmailAddress = EmailAddress(client.value)
  }
  implicit object UserNameDomainAdapter extends DomainAdapter[ClientDomain.UserName, UserName] {
    def toDomain(client: ClientDomain.UserName): UserName = UserName(client.value)
  }
  implicit object PasswordDomainAdapter extends DomainAdapter[ClientDomain.Password, Password] {
    def toDomain(client: ClientDomain.Password): Password = Password(client.value)
  }
  implicit object UserIdDomainAdapter extends DomainAdapter[ClientDomain.User.Id, User.Id] {
    def toDomain(client: ClientDomain.User.Id): User.Id = User.Id(client.value)
  }

  // UserDomain to ClientDomain adapters
  trait ClientAdapter[D <: Domain, C <: ClientDomain] {
    def toClient(dom: D): C
  }

  object ClientAdapter {
    def apply[D <: Domain, C <: ClientDomain](
      implicit adapter: ClientAdapter[D, C]
    ): ClientAdapter[D, C] = adapter
  }

  implicit class ClientAdapterGen[D <: Domain, C <: ClientDomain](dom: D)(implicit adapter: ClientAdapter[D, C]) {
    def toClient():C = adapter.toClient(dom)
  }

  implicit object EmailClientAdapter extends ClientAdapter[EmailAddress, ClientDomain.EmailAddress] {
    def toClient(dom: EmailAddress): ClientDomain.EmailAddress = ClientDomain.EmailAddress(dom.value)
  }
  implicit object UserNameClientAdapter extends ClientAdapter[UserName, ClientDomain.UserName] {
    def toClient(dom: UserName): ClientDomain.UserName = ClientDomain.UserName(dom.value)
  }
  implicit object DoneClientAdapter extends ClientAdapter[Done, ClientDomain.Done] {
    def toClient(dom: Done): ClientDomain.Done = ClientDomain.Done()
  }
  implicit object UserIdClientAdapter extends ClientAdapter[User.Id, ClientDomain.User.Id] {
    def toClient(dom: User.Id): ClientDomain.User.Id = ClientDomain.User.Id(dom.value)
  }
  implicit object UserClientAdapter extends ClientAdapter[User, ClientDomain.User] {
    def toClient(dom: User): ClientDomain.User = ClientDomain.User(
      id = dom.id.toClient,
      userName = dom.userName.toClient,
      emailAddress = dom.emailAddress.toClient
    )
  }
}
